import Foundation
import shared

struct BingoFigureSelectionScreenState {
    var gameId: Int64 = 0
    var cardGrid: [[BingoCell]] = []
    var isLoading: Bool = false
    var isUpdating: Bool = false
    var errorMessage: String? = nil
    var gameReady: Bool = false
    var selectedFigure: WinCondition? = nil
    var customPattern: Set<BingoCellPos> = []
}
