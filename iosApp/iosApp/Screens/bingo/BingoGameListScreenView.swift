import SwiftUI

struct BingoGameListScreenView: View {
    weak var coordinator: AppNavCoordinator?
    
    var body: some View {
        VStack {
            Text("Bingo Game List")
                .font(.largeTitle)
            
            Spacer()
            
            Button("New Game") {
                coordinator?.goToBingoGameSetup()
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

#Preview {
    BingoGameListScreenView(coordinator: nil)
}