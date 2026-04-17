package co.jarias.flexapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

actual class PreferencesManager(private val context: Context) {
    companion object {
        private val LAST_TOOL_KEY = stringPreferencesKey("last_tool")
    }

    actual suspend fun getLastTool(): ToolType? {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_TOOL_KEY]?.let { value ->
                try {
                    ToolType.valueOf(value)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }.firstOrNull()
    }

    actual suspend fun setLastTool(tool: ToolType) {
        context.dataStore.edit { preferences ->
            preferences[LAST_TOOL_KEY] = tool.name
        }
    }
}