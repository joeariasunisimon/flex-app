import Foundation

struct BingoCardScannerScreenState {
    var gameId: Int64 = 0
    var isLoading: Bool = false
    var isProcessing: Bool = false
    var detectedGrid: [[String]]? = nil
    var errorMessage: String? = nil
    var scanErrorMessage: String? = nil
    var cardSaved: Bool = false
    var navigateBack: Bool = false
}
