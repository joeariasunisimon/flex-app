import SwiftUI

struct WelcomeScreenView: View {
    @StateObject private var viewModel = WelcomeScreenViewModel()
    private let onNavigate: (NavigationEvent) -> Void
    
    init(coordinator: AppNavCoordinator?) {
        self.onNavigate = { event in
            coordinator?.handleNavigationEvent(event)
        }
    }
    
    var body: some View {
        VStack {
            Text("Welcome Screen")
                .font(.largeTitle)
            
            Text("FlexApp Bingo Tracker")
                .font(.subheadline)
            
            Spacer()
            
            Button("Get Started") {
                viewModel.onEvent(.onGetStartedClicked)
                onNavigate(.navigateToToolSelection)
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

#Preview {
    WelcomeScreenView(coordinator: nil)
}
