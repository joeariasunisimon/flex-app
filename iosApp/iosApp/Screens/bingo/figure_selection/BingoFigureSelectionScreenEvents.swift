import Foundation
import shared

enum BingoFigureSelectionScreenEvents {
    case onFigureSelected(figure: WinCondition)
    case onCustomPatternToggled(row: Int, col: Int)
    case onContinue
}
