import Foundation
import shared

struct BingoGamePlayScreenState {
    var gameState: GameState? = nil
    var isLoading: Bool = false
    var errorMessage: String? = nil
    var showWinDialog: Bool = false
    var lastMarkedNumber: Int32? = nil
}
