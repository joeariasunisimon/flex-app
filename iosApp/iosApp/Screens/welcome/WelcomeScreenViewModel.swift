import Foundation
import shared

class WelcomeScreenViewModel: ObservableObject {
    @Published var state = WelcomeScreenState()
    private let getLastToolUseCase: GetLastToolUseCase
    
    init() {
        self.getLastToolUseCase = KoinHelper().getLastToolUseCase
        loadLastTool()
    }
    
    private func loadLastTool() {
        getLastToolUseCase.invoke { [weak self] tool, error in
            DispatchQueue.main.async {
                self?.state.lastTool = tool
                self?.state.isLoading = false
            }
        }
    }
    
    func onEvent(_ event: WelcomeScreenEvents, onNavigate: (NavigationEvent) -> Void) {
        switch event {
        case .onGetStartedClicked:
            if let lastTool = state.lastTool {
                onNavigate(.navigateToTool(toolType: lastTool))
            } else {
                onNavigate(.navigateToToolSelection)
            }
        }
    }
}
