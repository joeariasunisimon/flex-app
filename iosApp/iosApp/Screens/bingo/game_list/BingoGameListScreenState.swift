import Foundation
import shared

struct BingoGameListScreenState {
    var games: [Game] = []
    var isLoading: Bool = true
    var errorMessage: String? = nil
    var pendingSetupGameIds: [Int64] = []
    var continueToCardSetup: Int64? = nil
    var continueToFigureSelection: Int64? = nil
    var continueToGamePlay: Int64? = nil
}
