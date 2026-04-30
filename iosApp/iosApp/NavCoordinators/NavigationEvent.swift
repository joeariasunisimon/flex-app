import Foundation

enum NavigationEvent: Equatable {
    case onNavigateUp
    case navigateToToolSelection
    case navigateToBingoGameList
    case navigateToBingoGameSetup
    case navigateToBingoCardSetup(gameId: Int64)
    case navigateToBingoCardScanner(gameId: Int64)
    case navigateToBingoFigureSelection(gameId: Int64)
    case navigateToBingoGamePlay(gameId: Int64)
}
