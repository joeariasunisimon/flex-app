import SwiftUI

struct BingoGamePlayScreenView: View {
    weak var coordinator: AppNavCoordinator?
    var gameId: Int
    
    var body: some View {
        VStack {
            Text("Play Bingo!")
                .font(.largeTitle)
            
            Spacer()
            
            Text("Game ID: \(gameId)")
                .font(.headline)
            
            Spacer()
            
            Button("Back to List") {
                coordinator?.goToBingoGameList()
            }
            .buttonStyle(.bordered)
        }
        .padding()
    }
}

#Preview {
    BingoGamePlayScreenView(coordinator: nil, gameId: 1)
}