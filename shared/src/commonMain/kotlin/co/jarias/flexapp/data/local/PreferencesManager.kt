package co.jarias.flexapp.data.local

expect class PreferencesManager {
    suspend fun getLastTool(): ToolType?
    suspend fun setLastTool(tool: ToolType)
    suspend fun getPendingSetupGameIds(): List<Long>
    suspend fun addPendingSetupGameId(gameId: Long)
    suspend fun removePendingSetupGameId(gameId: Long)
    suspend fun clearPendingSetupGameIds()

    suspend fun getCardSetupState(gameId: Long): CardSetupState?
    suspend fun setCardSetupState(gameId: Long, state: CardSetupState)
    suspend fun clearCardSetupState(gameId: Long)
}

data class CardSetupState(
    val currentColumn: Int,
    val cardNumbers: List<List<String>>
)

enum class ToolType {
    BINGO,
    SUDOKU
}