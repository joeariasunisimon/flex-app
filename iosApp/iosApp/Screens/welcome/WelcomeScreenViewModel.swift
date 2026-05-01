import Foundation
import shared

class WelcomeScreenViewModel: ObservableObject {
    @Published var state = WelcomeScreenState()
    private let preferencesManager: PreferencesManager
    
    init() {
        self.preferencesManager = KoinHelper().preferencesManager
        loadLastTool()
    }
    
    private func loadLastTool() {
        preferencesManager.getLastTool { [weak self] tool, error in
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
