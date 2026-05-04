import Foundation
import shared

class BingoFigureSelectionScreenViewModel: ObservableObject {
    @Published var state: BingoFigureSelectionScreenState
    
    private let getBingoCardUseCase: GetBingoCardUseCase
    private let updateGameFigureUseCase: UpdateGameFigureUseCase
    
    init(getBingoCardUseCase: GetBingoCardUseCase, updateGameFigureUseCase: UpdateGameFigureUseCase, gameId: Int64) {
        self.getBingoCardUseCase = getBingoCardUseCase
        self.updateGameFigureUseCase = updateGameFigureUseCase
        self.state = BingoFigureSelectionScreenState(gameId: gameId)
        loadGame(gameId: gameId)
    }
    
    private func loadGame(gameId: Int64) {
        state.isLoading = true
        getBingoCardUseCase.invoke(gameId: gameId) { [weak self] card, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                self.state.isLoading = false
                if let card = card {
                    self.state.cardGrid = card.grid
                } else {
                    self.state.errorMessage = "No card found"
                }
            }
        }
    }
    
    func onEvent(_ event: BingoFigureSelectionScreenEvents) {
        switch event {
        case .onFigureSelected(let figure):
            state.selectedFigure = figure
            state.customPattern = []
        case .onCustomPatternToggled(let row, let col):
            let pos = BingoCellPos(row: Int32(row), col: Int32(col))
            if state.customPattern.contains(pos) {
                state.customPattern.remove(pos)
            } else {
                state.customPattern.insert(pos)
            }
            state.selectedFigure = nil
        case .onContinue:
            saveFigure()
        }
    }
    
    private func saveFigure() {
        if state.selectedFigure == nil && state.customPattern.isEmpty {
            state.errorMessage = "Please select a figure or create a custom pattern"
            return
        }
        
        state.isUpdating = true
        let winCondition = state.selectedFigure ?? WinConditionCustom(displayName: "Custom", requiredCells: state.customPattern)
        
        updateGameFigureUseCase.invoke(gameId: state.gameId, winCondition: winCondition) { [weak self] error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                self.state.isUpdating = false
                if let error = error {
                    self.state.errorMessage = "Failed to save figure: \(error.localizedDescription)"
                } else {
                    self.state.gameReady = true
                }
            }
        }
    }
}
