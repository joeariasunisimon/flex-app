import Foundation
import shared
import Combine

class BingoGameListScreenViewModel: ObservableObject {
    private let getAllGamesUseCase: GetAllGamesUseCase
    private let restartGameUseCase: RestartGameUseCase
    private let dropGameUseCase: DropGameUseCase
    private let checkGameSetupStatusUseCase: CheckGameSetupStatusUseCase
    private let getPendingSetupGameIdsUseCase: GetPendingSetupGameIdsUseCase
    private let removePendingSetupGameIdUseCase: RemovePendingSetupGameIdUseCase
    
    @Published var state = BingoGameListScreenState()
    
    private var gamesWatcher: FlowWatcher<NSArray>?
    
    init(
        getAllGamesUseCase: GetAllGamesUseCase,
        restartGameUseCase: RestartGameUseCase,
        dropGameUseCase: DropGameUseCase,
        checkGameSetupStatusUseCase: CheckGameSetupStatusUseCase,
        getPendingSetupGameIdsUseCase: GetPendingSetupGameIdsUseCase,
        removePendingSetupGameIdUseCase: RemovePendingSetupGameIdUseCase
    ) {
        self.getAllGamesUseCase = getAllGamesUseCase
        self.restartGameUseCase = restartGameUseCase
        self.dropGameUseCase = dropGameUseCase
        self.checkGameSetupStatusUseCase = checkGameSetupStatusUseCase
        self.getPendingSetupGameIdsUseCase = getPendingSetupGameIdsUseCase
        self.removePendingSetupGameIdUseCase = removePendingSetupGameIdUseCase
        
        observeGames()
        refreshPendingIds()
    }
    
    deinit {
        gamesWatcher?.close()
    }
    
    private func observeGames() {
        state.isLoading = true
        let helper = KoinHelper()
        let watcher = helper.getAllGamesWatcher()
        gamesWatcher = watcher
        watcher.watch { [weak self] games in
            DispatchQueue.main.async {
                self?.state.isLoading = false
                if let gamesList = games as? [Game] {
                    self?.state.games = gamesList
                }
            }
        }
    }
    
    private func refreshPendingIds() {
        getPendingSetupGameIdsUseCase.invoke { [weak self] ids, error in
            if let ids = ids {
                DispatchQueue.main.async {
                    self?.state.pendingSetupGameIds = ids.map { $0.int64Value }
                }
            }
        }
    }
    
    func onEvent(_ event: BingoGameListScreenEvents) {
        switch event {
        case .onCreateNewGameClicked:
            break
        case .onPlayGame(_):
            break
        case .onRestartGame(let gameId):
            restartGame(gameId: gameId)
        case .onDeleteGame(let gameId):
            deleteGame(gameId: gameId)
        case .onRetryClicked:
            observeGames()
            refreshPendingIds()
        case .onContinueSetup(let gameId):
            continueSetup(gameId: gameId)
        }
    }
    
    private func continueSetup(gameId: Int64) {
        checkGameSetupStatusUseCase.invoke(gameId: gameId) { [weak self] status, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                if let status = status {
                    switch status {
                    case is GameSetupStatus.CardSetupRequired:
                        self.state.continueToCardSetup = gameId
                    case is GameSetupStatus.FigureSelectionRequired:
                        self.state.continueToFigureSelection = gameId
                    case is GameSetupStatus.SetupComplete:
                        self.state.continueToGamePlay = gameId
                        self.refreshPendingIds()
                    default:
                        break
                    }
                }
            }
        }
    }
    
    private func restartGame(gameId: Int64) {
        restartGameUseCase.invoke(gameId: gameId) { [weak self] error in
            self?.removePendingSetupGameIdUseCase.invoke(gameId: gameId) { error in
                self?.refreshPendingIds()
            }
        }
    }
    
    private func deleteGame(gameId: Int64) {
        dropGameUseCase.invoke(gameId: gameId) { [weak self] error in
            self?.removePendingSetupGameIdUseCase.invoke(gameId: gameId) { error in
                self?.refreshPendingIds()
            }
        }
    }
}
