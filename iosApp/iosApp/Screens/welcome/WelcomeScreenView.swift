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
        ZStack {
            Color.primaryTeal
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Top Content
                VStack(spacing: 16) {
                    Text("FlexApp")
                        .font(.system(size: 56, weight: .bold, design: .serif))
                        .foregroundColor(.white)
                        .padding(.top, 60)
                    
                    Text("Your Multi-Purpose Gaming Companion")
                        .font(.title3)
                        .fontWeight(.semibold)
                        .multilineTextAlignment(.center)
                        .foregroundColor(.white)
                        .padding(.horizontal, 40)
                    
                    Text("Experience the joy of classic games with modern technology. Bingo, and more games to come!")
                        .font(.body)
                        .multilineTextAlignment(.center)
                        .foregroundColor(.white.opacity(0.9))
                        .padding(.top, 40)
                        .padding(.horizontal, 40)
                }
                
                Spacer()
                
                // Bottom Button
                Button(action: {
                    viewModel.onEvent(.onGetStartedClicked, onNavigate: onNavigate)
                }) {
                    HStack {
                        if viewModel.state.isLoading || viewModel.state.lastTool != nil {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("Get Started")
                                .font(.headline)
                                .fontWeight(.bold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 60)
                    .background(Color.accentAmber)
                    .foregroundColor(.white)
                    .cornerRadius(30)
                }
                .padding(.horizontal, 32)
                .padding(.bottom, 50)
                .disabled(viewModel.state.isLoading || viewModel.state.lastTool != nil)
            }
        }
        .onAppear {
            if !viewModel.state.isLoading, let lastTool = viewModel.state.lastTool {
                onNavigate(.navigateToTool(toolType: lastTool))
            }
        }
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
