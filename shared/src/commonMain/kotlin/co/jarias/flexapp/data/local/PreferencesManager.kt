package co.jarias.flexapp.data.local

expect class PreferencesManager {
    suspend fun getLastTool(): ToolType?
    suspend fun setLastTool(tool: ToolType)
}

enum class ToolType {
    BINGO,
    SUDOKU
}