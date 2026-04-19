import SwiftUI

struct WelcomeScreenView: View {
    weak var coordinator: AppNavCoordinator?
    
    var body: some View {
        VStack {
            Text("Welcome Screen")
                .font(.largeTitle)
            
            Spacer()
            
            Button("Get Started") {
                coordinator?.goToToolSelection()
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

#Preview {
    WelcomeScreenView(coordinator: nil)
}