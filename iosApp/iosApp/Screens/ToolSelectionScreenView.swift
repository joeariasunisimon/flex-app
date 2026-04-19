import SwiftUI

struct ToolSelectionScreenView: View {
    weak var coordinator: AppNavCoordinator?
    
    var body: some View {
        VStack {
            Text("Tool Selection")
                .font(.largeTitle)
            
            Spacer()
            
            Button("Select Bingo") {
                coordinator?.goToBingoGameList()
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

#Preview {
    ToolSelectionScreenView(coordinator: nil)
}