import Foundation

enum BingoCardSetupScreenEvents {
    case onCardNumberChanged(row: Int, col: Int, value: String)
    case onNextColumn
    case onPreviousColumn
    case onSaveCard
    case onRandomFill
}
