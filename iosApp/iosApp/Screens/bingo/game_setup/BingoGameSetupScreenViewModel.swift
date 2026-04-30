import Foundation
import shared

class BingoGameSetupScreenViewModel: ObservableObject {
    private let createGameUseCase: CreateGameUseCase
    
    @Published var state = BingoGameSetupScreenState()
    
    init(createGameUseCase: CreateGameUseCase) {
        self.createGameUseCase = createGameUseCase
    }
    
    func onEvent(_ event: BingoGameSetupScreenEvents) {
        switch event {
        case .onNameChanged(let name):
            state.name = name
        case .onCreateClicked:
            createGame()
        }
    }
    
    private func createGame() {
        state.isCreating = true
        createGameUseCase.invoke(name: state.name, targetFigure: nil) { game, error in
            DispatchQueue.main.async {
                self.state.isCreating = false
                if let game = game {
                    self.state.createdGameId = game.id?.int64Value
                } else if let error = error {
                    self.state.error = error.localizedDescription
                }
            }
        }
    }
}
