import SwiftUI

struct BingoFigureSelectionScreenView: View {
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
            Text("Bingo Figure Selection")
                .font(.largeTitle)
            
            Spacer()
            
            Button("Finish Setup") {
                onNavigate(.navigateToBingoGamePlay(gameId: gameId))
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

#Preview {
    BingoFigureSelectionScreenView(coordinator: nil, gameId: 1)
}
