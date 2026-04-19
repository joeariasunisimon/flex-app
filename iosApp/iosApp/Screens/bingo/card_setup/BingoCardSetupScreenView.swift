import SwiftUI

struct BingoCardSetupScreenView: View {
    weak var coordinator: AppNavCoordinator?
    var gameId: Int
    
    var body: some View {
        VStack {
            Text("Bingo Card Setup")
                .font(.largeTitle)
            
            Spacer()
            
            Text("Step 1 of 5: Fill column B")
                .font(.headline)
            
            Spacer()
            
            Button("Next") {
                coordinator?.goToBingoFigureSelection(gameId: gameId)
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

#Preview {
    BingoCardSetupScreenView(coordinator: nil, gameId: 1)
}