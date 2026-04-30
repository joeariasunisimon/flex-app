import SwiftUI
import shared

struct BingoGameListScreenView: View {
    @StateObject private var viewModel: BingoGameListScreenViewModel
    private let onNavigate: (NavigationEvent) -> Void
    
    init(coordinator: AppNavCoordinator) {
        let helper = KoinHelper()
        _viewModel = StateObject(wrappedValue: BingoGameListScreenViewModel(
            getAllGamesUseCase: helper.getAllGamesUseCase,
            restartGameUseCase: helper.restartGameUseCase,
            dropGameUseCase: helper.dropGameUseCase,
            bingoCardRepository: helper.bingoCardRepository,
            gameRepository: helper.gameRepository,
            preferencesManager: helper.preferencesManager
        ))
        self.onNavigate = { event in
            coordinator.handleNavigationEvent(event)
        }
    }
    
    var body: some View {
        BingoGameListContent(
            state: viewModel.state,
            onEvent: viewModel.onEvent,
            onNavigate: onNavigate
        )
        .navigationTitle("Bingo Games")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    onNavigate(.navigateToBingoGameSetup)
                }) {
                    Image(systemName: "plus")
                }
            }
        }
    }
}

struct BingoGameListContent: View {
    let state: BingoGameListScreenState
    let onEvent: (BingoGameListScreenEvents) -> Void
    let onNavigate: (NavigationEvent) -> Void
    
    var body: some View {
        Group {
            if state.isLoading {
                ProgressView()
            } else if let error = state.errorMessage {
                VStack {
                    Text(error).foregroundColor(.red)
                    Button("Retry") { onEvent(.onRetryClicked) }
                }
            } else if state.games.isEmpty {
                Text("No games found. Create one!")
            } else {
                List(state.games, id: \.id) { game in
                    GameRow(game: game, onEvent: onEvent, onNavigate: onNavigate)
                }
            }
        }
        .onChange(of: state.continueToCardSetup) { _, gameId in
            if let id = gameId { onNavigate(.navigateToBingoCardSetup(gameId: id)) }
        }
        .onChange(of: state.continueToFigureSelection) { _, gameId in
            if let id = gameId { onNavigate(.navigateToBingoFigureSelection(gameId: id)) }
        }
        .onChange(of: state.continueToGamePlay) { _, gameId in
            if let id = gameId { onNavigate(.navigateToBingoGamePlay(gameId: id)) }
        }
    }
}

struct GameRow: View {
    let game: Game
    let onEvent: (BingoGameListScreenEvents) -> Void
    let onNavigate: (NavigationEvent) -> Void
    
    var body: some View {
        VStack(alignment: .leading) {
            Text(game.name)
                .font(.headline)
            Text("Created: \(game.createdAt)")
                .font(.caption)
        }
        .contentShape(Rectangle())
        .onTapGesture {
            if let id = game.id?.int64Value {
                onNavigate(.navigateToBingoGamePlay(gameId: id))
            }
        }
        .swipeActions {
            Button(role: .destructive) {
                if let id = game.id?.int64Value {
                    onEvent(.onDeleteGame(gameId: id))
                }
            } label: {
                Label("Delete", systemImage: "trash")
            }
        }
    }
}
