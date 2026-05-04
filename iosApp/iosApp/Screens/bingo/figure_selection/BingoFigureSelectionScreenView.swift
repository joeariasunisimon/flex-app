import SwiftUI
import shared

struct BingoFigureSelectionScreenView: View {
    @StateObject private var viewModel: BingoFigureSelectionScreenViewModel
    private let onNavigate: (NavigationEvent) -> Void
    
    init(coordinator: AppNavCoordinator?, gameId: Int64) {
        let helper = KoinHelper()
        _viewModel = StateObject(wrappedValue: BingoFigureSelectionScreenViewModel(
            getBingoCardUseCase: helper.getBingoCardUseCase,
            updateGameFigureUseCase: helper.updateGameFigureUseCase,
            gameId: gameId
        ))
        self.onNavigate = { event in
            coordinator?.handleNavigationEvent(event)
        }
    }
    
    var body: some View {
        ZStack {
            Color.backgroundWarm.ignoresSafeArea()
            
            VStack(spacing: 0) {
                if viewModel.state.isLoading {
                    Spacer()
                    ProgressView().tint(.primaryTeal)
                    Spacer()
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 24) {
                            Text("Choose your target figure")
                                .font(.title3)
                                .fontWeight(.bold)
                            
                            // Predefined Figures
                            VStack(spacing: 12) {
                                FigureRow(title: "Column B", isSelected: viewModel.state.selectedFigure?.displayName == "B") {
                                    viewModel.onEvent(.onFigureSelected(figure: WinCondition.companion.B))
                                }
                                FigureRow(title: "Column I", isSelected: viewModel.state.selectedFigure?.displayName == "I") {
                                    viewModel.onEvent(.onFigureSelected(figure: WinCondition.companion.I))
                                }
                                FigureRow(title: "Column N", isSelected: viewModel.state.selectedFigure?.displayName == "N") {
                                    viewModel.onEvent(.onFigureSelected(figure: WinCondition.companion.N))
                                }
                                FigureRow(title: "Column G", isSelected: viewModel.state.selectedFigure?.displayName == "G") {
                                    viewModel.onEvent(.onFigureSelected(figure: WinCondition.companion.G))
                                }
                                FigureRow(title: "Column O", isSelected: viewModel.state.selectedFigure?.displayName == "O") {
                                    viewModel.onEvent(.onFigureSelected(figure: WinCondition.companion.O))
                                }
                                FigureRow(title: "Full Card", isSelected: viewModel.state.selectedFigure?.displayName == "Full Card") {
                                    viewModel.onEvent(.onFigureSelected(figure: WinCondition.companion.FULL_CARD))
                                }
                            }
                            
                            Text("Or create a custom pattern by tapping the grid below:")
                                .font(.subheadline)
                                .foregroundColor(.gray)
                            
                            // Preview Grid for Custom Pattern
                            BingoGridPreview(
                                grid: viewModel.state.cardGrid,
                                customPattern: viewModel.state.customPattern,
                                onCellTapped: { row, col in
                                    viewModel.onEvent(.onCustomPatternToggled(row: row, col: col))
                                }
                            )
                            
                            if let error = viewModel.state.errorMessage {
                                Text(error)
                                    .font(.caption)
                                    .foregroundColor(.red)
                                    .padding()
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .background(Color.red.opacity(0.1))
                                    .cornerRadius(8)
                            }
                        }
                        .padding(24)
                    }
                    
                    // Bottom Button
                    Button(action: { viewModel.onEvent(.onContinue) }) {
                        ZStack {
                            if viewModel.state.isUpdating {
                                ProgressView().tint(.white)
                            } else {
                                Text("Finish Setup")
                                    .fontWeight(.bold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color.primaryTeal)
                        .foregroundColor(.white)
                        .cornerRadius(28)
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 32)
                    .disabled(viewModel.state.isUpdating)
                }
            }
        }
        .navigationTitle("Figure Selection")
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
        .onChange(of: viewModel.state.gameReady) { _, ready in
            if ready {
                onNavigate(.navigateToBingoGamePlay(gameId: viewModel.state.gameId))
            }
        }
    }
}

struct FigureRow: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                Text(title)
                    .fontWeight(.medium)
                Spacer()
                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.primaryTeal)
                } else {
                    Image(systemName: "circle")
                        .foregroundColor(.gray.opacity(0.5))
                }
            }
            .padding()
            .background(isSelected ? Color.primaryTeal.opacity(0.1) : Color.white)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isSelected ? Color.primaryTeal : Color.gray.opacity(0.2), lineWidth: 1)
            )
        }
        .foregroundColor(.black)
    }
}

struct BingoGridPreview: View {
    let grid: [[BingoCell]]
    let customPattern: Set<BingoCellPos>
    let onCellTapped: (Int, Int) -> Void
    
    var body: some View {
        VStack(spacing: 4) {
            ForEach(0..<5, id: \.self) { row in
                HStack(spacing: 4) {
                    ForEach(0..<5, id: \.self) { col in
                        let isSelected = customPattern.contains(BingoCellPos(row: Int32(row), col: Int32(col)))
                        let cell = grid.indices.contains(row) && grid[row].indices.contains(col) ? grid[row][col] : nil
                        
                        ZStack {
                            Rectangle()
                                .fill(isSelected ? Color.accentAmber : Color.white)
                                .aspectRatio(1, contentMode: .fit)
                                .cornerRadius(4)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 4)
                                        .stroke(Color.gray.opacity(0.2), lineWidth: 1)
                                )
                            
                            if let num = cell?.number {
                                Text("\(num.intValue)")
                                    .font(.system(size: 10))
                                    .foregroundColor(isSelected ? .white : .black)
                            } else if cell?.isFree == true {
                                Text("FREE")
                                    .font(.system(size: 8, weight: .bold))
                                    .foregroundColor(.gray)
                            }
                        }
                        .onTapGesture {
                            onCellTapped(row, col)
                        }
                    }
                }
            }
        }
        .padding(8)
        .background(Color.white)
        .cornerRadius(8)
        .shadow(color: .black.opacity(0.05), radius: 2)
    }
}
