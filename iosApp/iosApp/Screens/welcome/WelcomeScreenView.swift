import SwiftUI
import shared

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
            if viewModel.state.isLoading {
                ProgressView()
            } else {
                Text("FlexApp")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                
                Text("Your Multi-Purpose Gaming Companion")
                    .font(.subheadline)
                
                Spacer()
                
                Button(action: {
                    viewModel.onEvent(.onGetStartedClicked)
                    if let lastTool = viewModel.state.lastTool {
                        onNavigate(.navigateToTool(toolType: lastTool))
                    } else {
                        onNavigate(.navigateToToolSelection)
                    }
                }) {
                    if viewModel.state.lastTool != nil {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("Get Started")
                            .fontWeight(.bold)
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(viewModel.state.lastTool != nil)
            }
        }
        .padding()
        .onChange(of: viewModel.state.isLoading) { _, isLoading in
            if !isLoading, let lastTool = viewModel.state.lastTool {
                onNavigate(.navigateToTool(toolType: lastTool))
            }
        }
    }
}

#Preview {
    WelcomeScreenView(coordinator: nil)
}
