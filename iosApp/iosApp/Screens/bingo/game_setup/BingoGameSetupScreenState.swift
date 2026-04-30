import Foundation

struct BingoGameSetupScreenState {
    var name: String = ""
    var isCreating: Bool = false
    var error: String? = nil
    var createdGameId: Int64? = nil
}
