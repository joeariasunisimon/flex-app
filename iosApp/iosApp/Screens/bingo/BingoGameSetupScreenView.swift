import SwiftUI

struct BingoGameSetupScreenView: View {
    weak var coordinator: AppNavCoordinator?
    
    var body: some View {
        VStack {
            Text("Bingo Game Setup")
                .font(.largeTitle)
            
            Spacer()
            
            TextField("Game Name", text: .constant(""))
                .textFieldStyle(.roundedBorder)
                .padding()
            
            Button("Create Game") {
                coordinator?.goToBingoCardSetup(gameId: 1)
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

#Preview {
    BingoGameSetupScreenView(coordinator: nil)
}