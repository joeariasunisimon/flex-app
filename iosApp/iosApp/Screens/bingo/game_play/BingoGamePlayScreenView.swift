import SwiftUI

struct BingoGamePlayScreenView: View {
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
            Text("Bingo Game Play")
                .font(.largeTitle)
            
            Spacer()
            
            Text("Game ID: \(gameId)")
            
            Spacer()
            
            Button("Exit Game") {
                onNavigate(.navigateToBingoGameList)
            }
            .buttonStyle(.bordered)
        }
        .padding()
    }
}

#Preview {
    BingoGamePlayScreenView(coordinator: nil, gameId: 1)
}
