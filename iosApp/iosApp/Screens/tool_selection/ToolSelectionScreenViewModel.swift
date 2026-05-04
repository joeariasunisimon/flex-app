import Foundation
import shared

class ToolSelectionScreenViewModel: ObservableObject {
    @Published var state = ToolSelectionScreenState()
    private let setLastToolUseCase: SetLastToolUseCase
    
    init() {
        self.setLastToolUseCase = KoinHelper().setLastToolUseCase
    }
    
    func onEvent(_ event: ToolSelectionScreenEvents) {
        switch event {
        case .onToolSelected(let tool):
            let toolType: ToolType = tool == "Bingo" ? .bingo : .sudoku
            setLastToolUseCase.invoke(tool: toolType) { error in
                // Handle error if needed
            }
        }
    }
}
