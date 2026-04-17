package co.jarias.flexapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Serializable
data class CardSetupStateData(
    val currentColumn: Int,
    val cardNumbers: List<List<String>>
)

actual class PreferencesManager(private val context: Context) {
    companion object {
        private val LAST_TOOL_KEY = stringPreferencesKey("last_tool")
        private val PENDING_SETUP_GAME_IDS_KEY = stringPreferencesKey("pending_setup_game_ids")
        private const val CARD_SETUP_STATE_PREFIX = "card_setup_state_"
    }

    private val json = Json { ignoreUnknownKeys = true }

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

    actual suspend fun getPendingSetupGameIds(): List<Long> {
        return context.dataStore.data.map { preferences ->
            preferences[PENDING_SETUP_GAME_IDS_KEY]
                ?.split(",")
                ?.mapNotNull { it.toLongOrNull() }
                ?: emptyList()
        }.firstOrNull() ?: emptyList()
    }

    actual suspend fun addPendingSetupGameId(gameId: Long) {
        context.dataStore.edit { preferences ->
            val current = preferences[PENDING_SETUP_GAME_IDS_KEY]
                ?.split(",")
                ?.mapNotNull { it.toLongOrNull() }
                ?.toMutableList()
                ?: mutableListOf()
            if (!current.contains(gameId)) {
                current.add(gameId)
                preferences[PENDING_SETUP_GAME_IDS_KEY] = current.joinToString(",")
            }
        }
    }

    actual suspend fun removePendingSetupGameId(gameId: Long) {
        context.dataStore.edit { preferences ->
            val current = preferences[PENDING_SETUP_GAME_IDS_KEY]
                ?.split(",")
                ?.mapNotNull { it.toLongOrNull() }
                ?.toMutableList()
                ?: mutableListOf()
            current.remove(gameId)
            preferences[PENDING_SETUP_GAME_IDS_KEY] = current.joinToString(",")
        }
    }

    actual suspend fun clearPendingSetupGameIds() {
        context.dataStore.edit { preferences ->
            preferences.remove(PENDING_SETUP_GAME_IDS_KEY)
        }
    }

    actual suspend fun getCardSetupState(gameId: Long): CardSetupState? {
        val key = stringPreferencesKey("$CARD_SETUP_STATE_PREFIX$gameId")
        return context.dataStore.data.map { preferences ->
            preferences[key]?.let { value ->
                try {
                    val data = json.decodeFromString<CardSetupStateData>(value)
                    CardSetupState(data.currentColumn, data.cardNumbers)
                } catch (e: Exception) {
                    null
                }
            }
        }.firstOrNull()
    }

    actual suspend fun setCardSetupState(gameId: Long, state: CardSetupState) {
        val key = stringPreferencesKey("$CARD_SETUP_STATE_PREFIX$gameId")
        val data = CardSetupStateData(state.currentColumn, state.cardNumbers)
        context.dataStore.edit { preferences ->
            preferences[key] = json.encodeToString(data)
        }
    }

    actual suspend fun clearCardSetupState(gameId: Long) {
        val key = stringPreferencesKey("$CARD_SETUP_STATE_PREFIX$gameId")
        context.dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}