import Foundation
import shared

class WelcomeScreenViewModel: ObservableObject {
    @Published var state = WelcomeScreenState()
    
    func onEvent(_ event: WelcomeScreenEvents) {
        switch event {
        case .onGetStartedClicked:
            // Handled by view navigating through coordinator
            break
        }
    }
}
