import Foundation
import shared

class BingoGamePlayScreenViewModel: ObservableObject {
    @Published var state = BingoGamePlayScreenState()
    
    private let getGameStateUseCase: GetGameStateUseCase
    private let markNumberUseCase: MarkNumberUseCase
    private let completeGameUseCase: CompleteGameUseCase
    private let gameId: Int64
    
    init(
        getGameStateUseCase: GetGameStateUseCase,
        markNumberUseCase: MarkNumberUseCase,
        completeGameUseCase: CompleteGameUseCase,
        gameId: Int64
    ) {
        self.getGameStateUseCase = getGameStateUseCase
        self.markNumberUseCase = markNumberUseCase
        self.completeGameUseCase = completeGameUseCase
        self.gameId = gameId
        loadGame()
    }
    
    func loadGame() {
        state.isLoading = true
        state.errorMessage = nil
        
        getGameStateUseCase.invoke(gameId: gameId) { [weak self] gameState, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                self.state.isLoading = false
                if let state = gameState {
                    self.state.gameState = state
                    self.state.showWinDialog = state.isWon
                } else if let error = error {
                    self.state.errorMessage = "Failed to load game: \(error.localizedDescription)"
                } else {
                    self.state.errorMessage = "Game not found"
                }
            }
        }
    }
    
    func onEvent(_ event: BingoGamePlayScreenEvents) {
        switch event {
        case .onNumberMarked(let number):
            markNumber(number: number)
        case .onWinDialogDismissed:
            state.showWinDialog = false
        case .onRetryClicked:
            loadGame()
        }
    }
    
    private func markNumber(number: Int32) {
        markNumberUseCase.invoke(gameId: gameId, number: number) { [weak self] success, error in
            guard let self = self else { return }
            if success?.boolValue == true {
                self.getGameStateUseCase.invoke(gameId: self.gameId) { [weak self] updatedGameState, error in
                    DispatchQueue.main.async {
                        guard let self = self else { return }
                        self.state.gameState = updatedGameState
                        self.state.lastMarkedNumber = number
                        self.state.showWinDialog = updatedGameState?.isWon == true
                        
                        if updatedGameState?.isWon == true {
                            self.completeGameUseCase.invoke(gameId: self.gameId) { error in }
                        }
                    }
                }
            }
        }
    }
}
