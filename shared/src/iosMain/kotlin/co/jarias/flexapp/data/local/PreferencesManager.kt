package co.jarias.flexapp.data.local

actual class PreferencesManager {
    actual suspend fun getLastTool(): ToolType? = null
    actual suspend fun setLastTool(tool: ToolType) { }
}