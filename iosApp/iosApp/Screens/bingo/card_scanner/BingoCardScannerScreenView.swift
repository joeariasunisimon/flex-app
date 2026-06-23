import SwiftUI
import VisionKit
import Vision
import shared

// MARK: - Document Camera Wrapper

struct DocumentCameraView: UIViewControllerRepresentable {
    let onResult: (UIImage) -> Void
    let onCancel: () -> Void

    func makeUIViewController(context: Context) -> VNDocumentCameraViewController {
        let controller = VNDocumentCameraViewController()
        controller.delegate = context.coordinator
        return controller
    }

    func updateUIViewController(_ uiViewController: VNDocumentCameraViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(onResult: onResult, onCancel: onCancel)
    }

    class Coordinator: NSObject, VNDocumentCameraViewControllerDelegate {
        let onResult: (UIImage) -> Void
        let onCancel: () -> Void

        init(onResult: @escaping (UIImage) -> Void, onCancel: @escaping () -> Void) {
            self.onResult = onResult
            self.onCancel = onCancel
        }

        func documentCameraViewController(_ controller: VNDocumentCameraViewController, didFinishWith scan: VNDocumentCameraScan) {
            guard scan.pageCount > 0 else {
                onCancel()
                return
            }
            let image = scan.imageOfPage(at: 0)
            onResult(image)
        }

        func documentCameraViewControllerDidCancel(_ controller: VNDocumentCameraViewController) {
            onCancel()
        }

        func documentCameraViewController(_ controller: VNDocumentCameraViewController, didFailWithError error: Error) {
            onCancel()
        }
    }
}

// MARK: - Main Scanner Screen

struct BingoCardScannerScreenView: View {
    @StateObject private var viewModel: BingoCardScannerScreenViewModel
    private let onNavigate: (NavigationEvent) -> Void

    @State private var showDocumentScanner = false
    @State private var isSimulatorError = false
    @State private var isRecognizing = false

    private let parser = BingoCardParser()

    init(coordinator: AppNavCoordinator?, gameId: Int64) {
        let helper = KoinHelper()
        _viewModel = StateObject(wrappedValue: BingoCardScannerScreenViewModel(
            saveBingoCardUseCase: helper.saveBingoCardUseCase,
            clearCardSetupStateUseCase: helper.clearCardSetupStateUseCase,
            gameId: gameId
        ))
        self.onNavigate = { event in
            coordinator?.handleNavigationEvent(event)
        }
    }

    var body: some View {
        ZStack {
            Color.backgroundWarm.ignoresSafeArea()

            if let grid = viewModel.state.detectedGrid {
                detectedGridView(grid: grid)
            } else {
                scanPromptView()
            }
        }
        .navigationTitle("Scan Bingo Card")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { onNavigate(.onNavigateUp) }) {
                    Image(systemName: "arrow.left")
                        .foregroundColor(.black)
                        .fontWeight(.bold)
                }
                .buttonStyle(.plain)
            }
        }
        .toolbarBackground(Color.backgroundWarm, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .sheet(isPresented: $showDocumentScanner) {
            // VNDocumentCameraViewController requires a physical device.
            // On simulator, show an error alert instead.
            if isSimulatorAvailable() {
                DocumentCameraView(
                    onResult: { image in
                        showDocumentScanner = false
                        processImage(image)
                    },
                    onCancel: {
                        showDocumentScanner = false
                    }
                )
                .ignoresSafeArea()
            } else {
                // Simulator fallback handled via alert
                EmptyView()
            }
        }
        .alert("Invalid Bingo Card", isPresented: .init(
            get: { viewModel.state.scanErrorMessage != nil },
            set: { if !$0 { viewModel.onEvent(.onErrorModalDismissed) } }
        )) {
            Button("Try Again") {
                viewModel.onEvent(.onErrorModalDismissed)
            }
        } message: {
            Text(viewModel.state.scanErrorMessage ?? "")
        }
        .alert("Camera Not Available", isPresented: $isSimulatorError) {
            Button("OK", role: .cancel) {}
        } message: {
            Text("The document scanner requires a physical device with a camera. Please run on a real device to scan Bingo cards.")
        }
        .onChange(of: viewModel.state.cardSaved) { _, saved in
            if saved {
                onNavigate(.navigateToBingoFigureSelection(gameId: viewModel.state.gameId))
            }
        }
    }

    // MARK: - Scan Prompt

    @ViewBuilder
    private func scanPromptView() -> some View {
        VStack(spacing: 0) {
            Spacer()

            Image(systemName: "camera.fill")
                .font(.system(size: 60))
                .foregroundColor(.primaryTeal)

            Spacer().frame(height: 24)

            Text("Scan Your Bingo Card")
                .font(.title2)
                .fontWeight(.bold)

            Spacer().frame(height: 12)

            Text("Position the camera over a 5×5 Bingo card.\nThe scanner will automatically crop and enhance the image.")
                .font(.body)
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            Spacer().frame(height: 32)

            Button(action: {
                if isSimulatorAvailable() {
                    viewModel.onEvent(.onStartScan)
                    showDocumentScanner = true
                } else {
                    isSimulatorError = true
                }
            }) {
                HStack(spacing: 8) {
                    if isRecognizing {
                        ProgressView()
                            .tint(.white)
                    } else {
                        Image(systemName: "camera.fill")
                        Text("Scan Card")
                            .fontWeight(.bold)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 56)
                .background(Color.primaryTeal)
                .foregroundColor(.white)
                .cornerRadius(12)
            }
            .disabled(isRecognizing)
            .padding(.horizontal, 32)

            if isRecognizing {
                Spacer().frame(height: 16)
                ProgressView()
                    .tint(.primaryTeal)
            }

            Spacer()
        }
    }

    // MARK: - Detected Grid View

    @ViewBuilder
    private func detectedGridView(grid: [[String]]) -> some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 24) {
                    Text("Card Detected!")
                        .font(.title2)
                        .fontWeight(.bold)
                        .padding(.top, 16)

                    BingoGridDisplay(grid: grid)

                    if let error = viewModel.state.errorMessage {
                        HStack {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundColor(.red)
                            Text(error)
                                .font(.caption)
                                .foregroundColor(.red)
                        }
                        .padding()
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.red.opacity(0.1))
                        .cornerRadius(8)
                    }

                    HStack(spacing: 16) {
                        Button(action: { viewModel.onEvent(.onRetry) }) {
                            Text("Retry")
                                .fontWeight(.bold)
                                .frame(maxWidth: .infinity)
                                .frame(height: 56)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(Color.primaryTeal, lineWidth: 1)
                                )
                                .foregroundColor(.primaryTeal)
                        }

                        Button(action: { viewModel.onEvent(.onConfirmSave) }) {
                            ZStack {
                                if viewModel.state.isProcessing {
                                    ProgressView()
                                        .tint(.white)
                                } else {
                                    Text("Confirm & Next")
                                        .fontWeight(.bold)
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(Color.primaryTeal)
                            .foregroundColor(.white)
                            .cornerRadius(12)
                        }
                        .disabled(viewModel.state.isProcessing)
                    }
                }
                .padding(.horizontal, 24)
            }
        }
    }

    // MARK: - Image Processing

    private func processImage(_ image: UIImage) {
        isRecognizing = true

        guard let cgImage = image.cgImage else {
            viewModel.onEvent(.onScanFailed(message: "Could not process the scanned image."))
            isRecognizing = false
            return
        }

        let request = VNRecognizeTextRequest { [self] request, error in
            DispatchQueue.main.async {
                self.isRecognizing = false

                if let error = error {
                    self.viewModel.onEvent(.onScanFailed(message: "Text recognition failed: \(error.localizedDescription)"))
                    return
                }

                guard let observations = request.results as? [VNRecognizedTextObservation], !observations.isEmpty else {
                    self.viewModel.onEvent(.onScanFailed(message: "No text detected in the image. Make sure the Bingo card is clearly visible."))
                    return
                }

                let imageSize = CGSize(width: cgImage.width, height: cgImage.height)
                let result = self.parser.parse(from: observations, imageSize: imageSize)

                switch result {
                case .success(let grid):
                    self.viewModel.onEvent(.onNumbersDetected(grid: grid))
                case .invalidRange(let number, let column):
                    self.viewModel.onEvent(.onScanFailed(
                        message: "Number \(number) is in the wrong column (\(column)). Should be: B(1-15), I(16-30), N(31-45), G(46-60), O(61-75)."
                    ))
                case .incomplete:
                    self.viewModel.onEvent(.onScanFailed(
                        message: "Could not detect a complete 5×5 Bingo card. Make sure all 24 numbers are clearly visible."
                    ))
                }
            }
        }

        request.recognitionLevel = .accurate
        request.usesLanguageCorrection = false

        let handler = VNImageRequestHandler(cgImage: cgImage, orientation: .up, options: [:])

        DispatchQueue.global(qos: .userInitiated).async {
            do {
                try handler.perform([request])
            } catch {
                DispatchQueue.main.async {
                    self.isRecognizing = false
                    self.viewModel.onEvent(.onScanFailed(message: "Image processing failed: \(error.localizedDescription)"))
                }
            }
        }
    }

    private func isSimulatorAvailable() -> Bool {
        #if targetEnvironment(simulator)
        return false
        #else
        return VNDocumentCameraViewController.isSupported
        #endif
    }
}

// MARK: - Bingo Grid Display

struct BingoGridDisplay: View {
    let grid: [[String]]
    private let columnLabels = ["B", "I", "N", "G", "O"]

    var body: some View {
        VStack(spacing: 4) {
            // Column headers
            HStack(spacing: 4) {
                ForEach(columnLabels, id: \.self) { label in
                    Text(label)
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(.primaryTeal)
                        .frame(maxWidth: .infinity)
                }
            }

            // Grid rows
            ForEach(0..<5, id: \.self) { row in
                HStack(spacing: 4) {
                    ForEach(0..<5, id: \.self) { col in
                        ZStack {
                            if row == 2 && col == 2 {
                                Text("FREE")
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(.gray)
                            } else {
                                Text(grid[row][col])
                                    .font(.body)
                                    .fontWeight(.bold)
                                    .foregroundColor(.black)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(row == 2 && col == 2 ? Color.gray.opacity(0.1) : Color.white)
                        .overlay(
                            Rectangle()
                                .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                        )
                    }
                }
            }
        }
        .padding(12)
        .background(Color.white)
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.gray.opacity(0.2), lineWidth: 1)
        )
    }
}

// MARK: - Previews

#Preview("Scan Prompt") {
    NavigationStack {
        BingoCardScannerScreenView(coordinator: nil, gameId: 1)
    }
}

#Preview("Error Modal") {
    NavigationStack {
        BingoCardScannerScreenView(coordinator: nil, gameId: 1)
    }
}

#Preview("Detected Grid") {
    NavigationStack {
        BingoCardScannerScreenView(coordinator: nil, gameId: 1)
    }
}
