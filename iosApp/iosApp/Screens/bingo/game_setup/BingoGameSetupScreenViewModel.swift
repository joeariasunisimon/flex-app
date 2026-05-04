import Foundation
import shared

class BingoGameSetupScreenViewModel: ObservableObject {
    private let createGameUseCase: CreateGameUseCase
    private let addPendingSetupGameIdUseCase: AddPendingSetupGameIdUseCase
    
    @Published var state = BingoGameSetupScreenState()
    
    init(createGameUseCase: CreateGameUseCase, addPendingSetupGameIdUseCase: AddPendingSetupGameIdUseCase) {
        self.createGameUseCase = createGameUseCase
        self.addPendingSetupGameIdUseCase = addPendingSetupGameIdUseCase
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
            if let gameId = game?.id?.int64Value {
                self.addPendingSetupGameIdUseCase.invoke(gameId: gameId) { error in
                    DispatchQueue.main.async {
                        self.state.isCreating = false
                        self.state.createdGameId = gameId
                    }
                }
            } else if let error = error {
                DispatchQueue.main.async {
                    self.state.isCreating = false
                    self.state.error = error.localizedDescription
                }
            }
        }
    }
}
