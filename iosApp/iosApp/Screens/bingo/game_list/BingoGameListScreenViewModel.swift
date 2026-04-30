import Foundation
import shared
import Combine

class BingoGameListScreenViewModel: ObservableObject {
    private let getAllGamesUseCase: GetAllGamesUseCase
    private let restartGameUseCase: RestartGameUseCase
    private let dropGameUseCase: DropGameUseCase
    private let bingoCardRepository: BingoCardRepository
    private let gameRepository: GameRepository
    private let preferencesManager: PreferencesManager
    
    @Published var state = BingoGameListScreenState()
    
    private var gamesWatcher: FlowWatcher<NSArray>?
    
    init(
        getAllGamesUseCase: GetAllGamesUseCase,
        restartGameUseCase: RestartGameUseCase,
        dropGameUseCase: DropGameUseCase,
        bingoCardRepository: BingoCardRepository,
        gameRepository: GameRepository,
        preferencesManager: PreferencesManager
    ) {
        self.getAllGamesUseCase = getAllGamesUseCase
        self.restartGameUseCase = restartGameUseCase
        self.dropGameUseCase = dropGameUseCase
        self.bingoCardRepository = bingoCardRepository
        self.gameRepository = gameRepository
        self.preferencesManager = preferencesManager
        
        observeGames()
        refreshPendingIds()
    }
    
    deinit {
        gamesWatcher?.close()
    }
    
    private func observeGames() {
        state.isLoading = true
        let helper = KoinHelper()
        // We use a specialized watcher to handle the Flow collection on iOS
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
        preferencesManager.getPendingSetupGameIds { [weak self] ids, error in
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
        bingoCardRepository.getCardsByGameId(gameId: gameId) { [weak self] cards, error in
            if let cards = cards {
                if cards.isEmpty {
                    DispatchQueue.main.async { self?.state.continueToCardSetup = gameId }
                } else {
                    self?.gameRepository.getGameById(gameId: gameId) { game, error in
                        if let game = game {
                            if game.targetFigure == nil {
                                DispatchQueue.main.async { self?.state.continueToFigureSelection = gameId }
                            } else {
                                self?.preferencesManager.removePendingSetupGameId(gameId: gameId) { error in
                                    DispatchQueue.main.async { self?.state.continueToGamePlay = gameId }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private func restartGame(gameId: Int64) {
        restartGameUseCase.invoke(gameId: gameId) { [weak self] error in
            self?.refreshPendingIds()
        }
    }
    
    private func deleteGame(gameId: Int64) {
        dropGameUseCase.invoke(gameId: gameId) { [weak self] error in
            self?.refreshPendingIds()
        }
    }
}
