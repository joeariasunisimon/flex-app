import SwiftUI
import shared

struct BingoGameSetupScreenView: View {
    @StateObject private var viewModel: BingoGameSetupScreenViewModel
    private let onNavigate: (NavigationEvent) -> Void
    
    init(coordinator: AppNavCoordinator?) {
        let helper = KoinHelper()
        _viewModel = StateObject(wrappedValue: BingoGameSetupScreenViewModel(
            createGameUseCase: helper.createGameUseCase
        ))
        self.onNavigate = { event in
            coordinator?.handleNavigationEvent(event)
        }
    }
    
    var body: some View {
        Form {
            Section(header: Text("Game Details")) {
                TextField("Game Name", text: Binding(
                    get: { viewModel.state.name },
                    set: { viewModel.onEvent(.onNameChanged(name: $0)) }
                ))
            }
            
            Button("Create Game") {
                viewModel.onEvent(.onCreateClicked)
            }
            .disabled(viewModel.state.name.isEmpty || viewModel.state.isCreating)
        }
        .navigationTitle("New Bingo Game")
        .onChange(of: viewModel.state.createdGameId) { _, gameId in
            if let id = gameId {
                onNavigate(.navigateToBingoCardSetup(gameId: id))
            }
        }
    }
}

#Preview {
    BingoGameSetupScreenView(coordinator: nil)
}
