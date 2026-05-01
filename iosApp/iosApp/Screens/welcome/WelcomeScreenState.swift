import Foundation
import shared

struct WelcomeScreenState {
    var isLoading: Bool = true
    var lastTool: ToolType? = nil
    var version: String = "1.0.0"
}
