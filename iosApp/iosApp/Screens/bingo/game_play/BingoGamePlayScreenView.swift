import SwiftUI
import shared

struct BingoGamePlayScreenView: View {
    @StateObject private var viewModel: BingoGamePlayScreenViewModel
    private let onNavigate: (NavigationEvent) -> Void
    
    init(coordinator: AppNavCoordinator?, gameId: Int64) {
        let helper = KoinHelper()
        _viewModel = StateObject(wrappedValue: BingoGamePlayScreenViewModel(
            getGameStateUseCase: helper.getGameStateUseCase,
            markNumberUseCase: helper.markNumberUseCase,
            completeGameUseCase: helper.completeGameUseCase,
            gameId: gameId
        ))
        self.onNavigate = { event in
            coordinator?.handleNavigationEvent(event)
        }
    }
    
    var body: some View {
        ZStack {
            Color.backgroundWarm.ignoresSafeArea()
            
            if viewModel.state.isLoading {
                ProgressView().tint(.primaryTeal)
            } else if let error = viewModel.state.errorMessage {
                VStack(spacing: 16) {
                    Text(error).foregroundColor(.red)
                    Button("Retry") { viewModel.onEvent(.onRetryClicked) }
                        .buttonStyle(.borderedProminent)
                }
            } else if let gameState = viewModel.state.gameState {
                VStack(spacing: 20) {
                    // Header
                    VStack(spacing: 4) {
                        Text(gameState.game.name)
                            .font(.title2)
                            .fontWeight(.bold)
                        Text("Target: \(gameState.game.targetFigure?.displayName ?? "Not set")")
                            .font(.subheadline)
                            .foregroundColor(.primaryTeal)
                    }
                    .padding(.top)
                    
                    // Game Grid
                    BingoGameGrid(
                        grid: gameState.card.grid,
                        markedNumbers: gameState.markedNumbers,
                        onCellTapped: { number in
                            if let num = number {
                                viewModel.onEvent(.onNumberMarked(number: num.int32Value))
                            }
                        }
                    )
                    .padding()
                    
                    Spacer()
                    
                    Button(action: { onNavigate(.onNavigateUp) }) {
                        Text("Exit Game")
                            .fontWeight(.bold)
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(Color.gray.opacity(0.1))
                            .foregroundColor(.black)
                            .cornerRadius(28)
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 32)
                }
            }
        }
        .navigationBarHidden(true)
        .alert("BINGO!", isPresented: Binding(
            get: { viewModel.state.showWinDialog },
            set: { if !$0 { viewModel.onEvent(.onWinDialogDismissed) } }
        )) {
            Button("Awesome!", role: .cancel) { }
        } message: {
            Text("Congratulations! You've matched the target figure.")
        }
    }
}

struct BingoGameGrid: View {
    let grid: [[BingoCell]]
    let markedNumbers: Set<KotlinInt>
    let onCellTapped: (KotlinInt?) -> Void
    
    var body: some View {
        VStack(spacing: 8) {
            // Letters Header
            HStack(spacing: 8) {
                ForEach(["B", "I", "N", "G", "O"], id: \.self) { letter in
                    Text(letter)
                        .font(.title3)
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity)
                        .foregroundColor(.primaryTeal)
                }
            }
            
            ForEach(0..<5, id: \.self) { row in
                HStack(spacing: 8) {
                    ForEach(0..<5, id: \.self) { col in
                        let cell = grid[row][col]
                        let isMarked = cell.isFree || (cell.number != nil && markedNumbers.contains(cell.number!))
                        
                        Button(action: { onCellTapped(cell.number) }) {
                            ZStack {
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(isMarked ? Color.accentAmber : Color.white)
                                    .aspectRatio(1, contentMode: .fit)
                                    .shadow(color: .black.opacity(0.05), radius: 2)
                                
                                if cell.isFree {
                                    Text("FREE")
                                        .font(.system(size: 10, weight: .bold))
                                        .foregroundColor(.white)
                                } else if let num = cell.number {
                                    Text("\(num.intValue)")
                                        .font(.title3)
                                        .fontWeight(.bold)
                                        .foregroundColor(isMarked ? .white : .black)
                                }
                            }
                        }
                        .disabled(cell.isFree || isMarked)
                    }
                }
            }
        }
        .padding(12)
        .background(Color.white.opacity(0.5))
        .cornerRadius(20)
    }
}
