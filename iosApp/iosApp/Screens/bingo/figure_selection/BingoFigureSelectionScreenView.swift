import SwiftUI

struct BingoFigureSelectionScreenView: View {
    weak var coordinator: AppNavCoordinator?
    var gameId: Int
    
    var body: some View {
        VStack {
            Text("Select Your Win Condition")
                .font(.largeTitle)
            
            Spacer()
            
            Button("Select Full Card") {
                coordinator?.goToBingoGamePlay(gameId: gameId)
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

#Preview {
    BingoFigureSelectionScreenView(coordinator: nil, gameId: 1)
}