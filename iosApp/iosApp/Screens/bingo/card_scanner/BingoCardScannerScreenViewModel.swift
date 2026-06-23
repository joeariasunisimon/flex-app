import Foundation
import shared

class BingoCardScannerScreenViewModel: ObservableObject {
    @Published var state: BingoCardScannerScreenState

    private let saveBingoCardUseCase: SaveBingoCardUseCase
    private let clearCardSetupStateUseCase: ClearCardSetupStateUseCase

    init(
        saveBingoCardUseCase: SaveBingoCardUseCase,
        clearCardSetupStateUseCase: ClearCardSetupStateUseCase,
        gameId: Int64
    ) {
        self.saveBingoCardUseCase = saveBingoCardUseCase
        self.clearCardSetupStateUseCase = clearCardSetupStateUseCase
        self.state = BingoCardScannerScreenState(gameId: gameId)
    }

    func onEvent(_ event: BingoCardScannerScreenEvents) {
        switch event {
        case .onNumbersDetected(let grid):
            state.detectedGrid = grid
            state.errorMessage = nil

        case .onScanFailed(let message):
            state.scanErrorMessage = message

        case .onStartScan:
            state.scanErrorMessage = nil
            state.isProcessing = false

        case .onErrorModalDismissed:
            state.scanErrorMessage = nil

        case .onConfirmSave:
            saveCard()

        case .onRetry:
            state.detectedGrid = nil
            state.errorMessage = nil
            state.scanErrorMessage = nil
        }
    }

    private func saveCard() {
        guard let gridStrings = state.detectedGrid else { return }

        state.isProcessing = true

        let grid: [[BingoCell]] = (0..<5).map { row in
            (0..<5).map { col in
                if row == 2 && col == 2 {
                    return BingoCell(number: nil, isMarked: false, isFree: true)
                } else {
                    let num = Int32(gridStrings[row][col]) ?? 0
                    return BingoCell(number: KotlinInt(value: num), isMarked: false, isFree: false)
                }
            }
        }

        let card = BingoCard(id: nil, grid: grid, createdAt: "")

        saveBingoCardUseCase.invoke(gameId: state.gameId, card: card) { [weak self] error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                if let error = error {
                    self.state.isProcessing = false
                    self.state.errorMessage = "Failed to save card: \(error.localizedDescription)"
                } else {
                    self.clearCardSetupStateUseCase.invoke(gameId: self.state.gameId) { _ in }
                    self.state.isProcessing = false
                    self.state.cardSaved = true
                }
            }
        }
    }
}
