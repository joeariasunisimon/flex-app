import Foundation

class ToolSelectionScreenViewModel: ObservableObject {
    @Published var state = ToolSelectionScreenState()
    
    func onEvent(_ event: ToolSelectionScreenEvents) {
        switch event {
        case .onToolSelected(_):
            break
        }
    }
}
