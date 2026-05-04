import SwiftUI
import shared

struct BingoGameSetupScreenView: View {
    @StateObject private var viewModel: BingoGameSetupScreenViewModel
    private let onNavigate: (NavigationEvent) -> Void
    
    init(coordinator: AppNavCoordinator?, viewModel: BingoGameSetupScreenViewModel? = nil) {
        let helper = KoinHelper()
        let vm = viewModel ?? BingoGameSetupScreenViewModel(
            createGameUseCase: helper.createGameUseCase
        )
        _viewModel = StateObject(wrappedValue: vm)
        self.onNavigate = { event in
            coordinator?.handleNavigationEvent(event)
        }
    }
    
    var body: some View {
        ZStack {
            Color.backgroundWarm.ignoresSafeArea()
            
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Game Name")
                            .font(.headline)
                            .fontWeight(.bold)
                        
                        TextField("Enter game name", text: Binding(
                            get: { viewModel.state.name },
                            set: { viewModel.onEvent(.onNameChanged(name: $0)) }
                        ))
                        .padding()
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                        )
                    }
                    
                    Text("You will set up your own Bingo card manually after creating the game.")
                        .font(.body)
                        .foregroundColor(.gray)
                    
                    if let error = viewModel.state.error {
                        HStack {
                            Text(error)
                                .font(.caption)
                                .foregroundColor(.red)
                                .padding(12)
                            Spacer()
                        }
                        .background(Color.red.opacity(0.1))
                        .cornerRadius(8)
                    }
                    
                    Button(action: {
                        viewModel.onEvent(.onCreateClicked)
                    }) {
                        HStack {
                            if viewModel.state.isCreating {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            } else {
                                Text("Create Game")
                                    .font(.title3)
                                    .fontWeight(.bold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color.primaryTeal)
                        .foregroundColor(.white)
                        .cornerRadius(28)
                    }
                    .disabled(viewModel.state.name.trimmingCharacters(in: .whitespaces).isEmpty || viewModel.state.isCreating)
                    .padding(.top, 8)
                }
                .padding(24)
            }
        }
        .navigationTitle("Create New Game")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { onNavigate(.onNavigateUp) }) {
                    Image(systemName: "arrow.left")
                        .foregroundColor(.black)
                        .fontWeight(.bold)
                }
                .buttonStyle(.plain)
            }
        }
        .toolbarBackground(Color.backgroundWarm, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .onChange(of: viewModel.state.createdGameId) { _, gameId in
            if let id = gameId {
                onNavigate(.navigateToBingoCardSetup(gameId: id))
            }
        }
    }
}

// MARK: - Previews

#Preview("Initial State") {
    NavigationStack {
        BingoGameSetupScreenView(coordinator: nil)
    }
}

#Preview("With Game Name") {
    let vm = BingoGameSetupScreenViewModel(createGameUseCase: KoinHelper().createGameUseCase)
    vm.state.name = "Family Bingo Night"
    return NavigationStack {
        BingoGameSetupScreenView(coordinator: nil, viewModel: vm)
    }
}

#Preview("Loading") {
    let vm = BingoGameSetupScreenViewModel(createGameUseCase: KoinHelper().createGameUseCase)
    vm.state.name = "Test Game"
    vm.state.isCreating = true
    return NavigationStack {
        BingoGameSetupScreenView(coordinator: nil, viewModel: vm)
    }
}

#Preview("Error") {
    let vm = BingoGameSetupScreenViewModel(createGameUseCase: KoinHelper().createGameUseCase)
    vm.state.name = "Test Game"
    vm.state.error = "Failed to create game. Please try again."
    return NavigationStack {
        BingoGameSetupScreenView(coordinator: nil, viewModel: vm)
    }
}
