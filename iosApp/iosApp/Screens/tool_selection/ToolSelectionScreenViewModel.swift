import Foundation
import shared

class ToolSelectionScreenViewModel: ObservableObject {
    @Published var state = ToolSelectionScreenState()
    private let preferencesManager: PreferencesManager
    
    init() {
        self.preferencesManager = KoinHelper().preferencesManager
    }
    
    func onEvent(_ event: ToolSelectionScreenEvents) {
        switch event {
        case .onToolSelected(let tool):
            if tool == "Bingo" {
                preferencesManager.setLastTool(tool: .bingo) { error in
                    // Handle error if needed
                }
            } else if tool == "Sudoku" {
                preferencesManager.setLastTool(tool: .sudoku) { error in
                    // Handle error if needed
                }
            }
        }
    }
}
