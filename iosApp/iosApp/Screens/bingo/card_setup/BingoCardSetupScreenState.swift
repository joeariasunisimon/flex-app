import Foundation
import shared

struct BingoCardSetupScreenState {
    var gameId: Int64 = 0
    var cardNumbers: [[String]] = Array(repeating: Array(repeating: "", count: 5), count: 5)
    var currentColumn: Int = 0
    var isLoading: Bool = false
    var isSaving: Bool = false
    var errorMessage: String? = nil
    var cardSaved: Bool = false
    
    let columnLabels = ["B", "I", "N", "G", "O"]
    let columnRanges = [1...15, 16...30, 31...45, 46...60, 61...75]
    
    var currentColumnLabel: String {
        guard currentColumn >= 0 && currentColumn < columnLabels.count else { return "" }
        return columnLabels[currentColumn]
    }
    
    var currentColumnRange: ClosedRange<Int> {
        guard currentColumn >= 0 && currentColumn < columnRanges.count else { return 1...15 }
        return columnRanges[currentColumn]
    }
}
