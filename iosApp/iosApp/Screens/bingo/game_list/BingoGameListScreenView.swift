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
        ZStack {
            Color.backgroundWarm.ignoresSafeArea()
            
            BingoGameListContent(
                state: viewModel.state,
                onEvent: viewModel.onEvent,
                onNavigate: onNavigate
            )
        }
        .navigationTitle("Your Bingo Games")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { onNavigate(.onNavigateUp) }) {
                    Image(systemName: "arrow.left")
                        .foregroundColor(.black)
                        .fontWeight(.bold)
                }
            }
            
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    onNavigate(.navigateToBingoGameSetup)
                }) {
                    Text("New Game")
                        .font(.subheadline)
                        .fontWeight(.bold)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(Color.primaryTeal)
                        .foregroundColor(.white)
                        .cornerRadius(20)
                }.buttonStyle(.plain)
            }
        }
        .toolbarBackground(Color.backgroundWarm, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
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
                    .tint(.primaryTeal)
            } else if let error = state.errorMessage {
                VStack(spacing: 16) {
                    Text(error)
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)
                    
                    Button(action: { onEvent(.onRetryClicked) }) {
                        Text("Retry")
                            .fontWeight(.bold)
                            .padding(.horizontal, 32)
                            .padding(.vertical, 12)
                            .background(Color.primaryTeal)
                            .foregroundColor(.white)
                            .cornerRadius(24)
                    }
                }
            } else if state.games.isEmpty {
                VStack(spacing: 24) {
                    VStack(spacing: 8) {
                        Text("No games yet")
                            .font(.title2)
                            .fontWeight(.bold)
                        
                        Text("Create your first Bingo game to get started!")
                            .font(.body)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 40)
                    }
                    
                    Button(action: { onNavigate(.navigateToBingoGameSetup) }) {
                        Text("Create Game")
                            .fontWeight(.bold)
                            .padding(.horizontal, 40)
                            .padding(.vertical, 14)
                            .background(Color.primaryTeal)
                            .foregroundColor(.white)
                            .cornerRadius(28)
                    }
                }
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(state.games, id: \.id) { game in
                            GameCard(
                                game: game,
                                pendingSetupGameIds: state.pendingSetupGameIds,
                                onEvent: onEvent,
                                onNavigate: onNavigate
                            )
                        }
                    }
                    .padding(16)
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

struct GameCard: View {
    let game: Game
    let pendingSetupGameIds: [Int64]
    let onEvent: (BingoGameListScreenEvents) -> Void
    let onNavigate: (NavigationEvent) -> Void
    
    @State private var showDeleteAlert = false
    
    var isPendingSetup: Bool {
        pendingSetupGameIds.contains(game.id?.int64Value ?? -1) || game.targetFigure == nil
    }
    
    var isReadyToPlay: Bool {
        !game.isCompleted && game.targetFigure != nil
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(game.name)
                        .font(.title3)
                        .fontWeight(.bold)
                    
                    let statusText = isPendingSetup ? "Setup incomplete" :
                                    (game.isCompleted ? "Game finished" : "In progress • Target: \(game.targetFigure?.displayName ?? "None")")
                    
                    let statusColor = isPendingSetup ? Color.red :
                                     (game.isCompleted ? Color.accentAmber : Color.primaryTeal)
                    
                    Text(statusText)
                        .font(.subheadline)
                        .foregroundColor(statusColor)
                }
                
                Spacer()
                
                if game.isCompleted {
                    Image(systemName: "checkmark.square.fill")
                        .foregroundColor(.green)
                        .background(Color.green.opacity(0.1))
                        .cornerRadius(4)
                        .font(.title3)
                }
            }
            
            Text("Created: \(String(game.createdAt.prefix(10)))")
                .font(.caption)
                .foregroundColor(.secondary)
            
            HStack(spacing: 12) {
                if isPendingSetup {
                    Button(action: {
                        if let id = game.id?.int64Value {
                            onEvent(.onContinueSetup(gameId: id))
                        }
                    }) {
                        Text("Continue Setup")
                            .font(.caption)
                            .fontWeight(.bold)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background(Color.primaryTeal)
                            .foregroundColor(.white)
                            .cornerRadius(18)
                    }
                } else if isReadyToPlay {
                    Button(action: {
                        if let id = game.id?.int64Value {
                            onEvent(.onRestartGame(gameId: id))
                        }
                    }) {
                        HStack {
                            Image(systemName: "arrow.clockwise.circle")
                            Text("Restart")
                        }
                        .font(.caption)
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 18)
                                .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                        )
                        .foregroundColor(.black)
                    }
                }
                
                Button(action: { showDeleteAlert = true }) {
                    HStack {
                        Image(systemName: "trash")
                        Text("Delete")
                    }
                    .font(.caption)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .overlay(
                        RoundedRectangle(cornerRadius: 18)
                            .stroke(Color.red.opacity(0.3), lineWidth: 1)
                    )
                    .foregroundColor(.red)
                }
            }
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.05), radius: 2, x: 0, y: 1)
        .onTapGesture {
            if isPendingSetup {
                if let id = game.id?.int64Value {
                    onEvent(.onContinueSetup(gameId: id))
                }
            } else {
                if let id = game.id?.int64Value {
                    onNavigate(.navigateToBingoGamePlay(gameId: id))
                }
            }
        }
        .alert("Delete Game?", isPresented: $showDeleteAlert) {
            Button("Delete", role: .destructive) {
                if let id = game.id?.int64Value {
                    onEvent(.onDeleteGame(gameId: id))
                }
            }
            Button("Cancel", role: .cancel) { }
        } message: {
            Text("Are you sure you want to delete \"\(game.name)\"? This cannot be undone.")
        }
    }
}

#Preview {
    NavigationStack {
        BingoGameListScreenView(coordinator: AppNavCoordinator())
    }
}
