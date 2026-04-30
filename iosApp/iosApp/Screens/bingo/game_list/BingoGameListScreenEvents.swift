import Foundation

enum BingoGameListScreenEvents {
    case onCreateNewGameClicked
    case onPlayGame(gameId: Int64)
    case onRestartGame(gameId: Int64)
    case onDeleteGame(gameId: Int64)
    case onRetryClicked
    case onContinueSetup(gameId: Int64)
}
