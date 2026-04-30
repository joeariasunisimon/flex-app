import SwiftUI

struct BingoCardSetupScreenView: View {
    private let onNavigate: (NavigationEvent) -> Void
    var gameId: Int64
    
    init(coordinator: AppNavCoordinator?, gameId: Int64) {
        self.onNavigate = { event in
            coordinator?.handleNavigationEvent(event)
        }
        self.gameId = gameId
    }
    
    var body: some View {
        VStack {
            Text("Bingo Card Setup")
                .font(.largeTitle)
            
            Spacer()
            
            Text("Step 1 of 5: Fill column B")
                .font(.headline)
            
            Spacer()
            
            Button("Next") {
                onNavigate(.navigateToBingoFigureSelection(gameId: gameId))
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

#Preview {
    BingoCardSetupScreenView(coordinator: nil, gameId: 1)
}
