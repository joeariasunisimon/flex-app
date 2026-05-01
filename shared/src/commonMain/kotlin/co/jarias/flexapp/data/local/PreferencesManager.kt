package co.jarias.flexapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CardSetupStateData(
    val currentColumn: Int,
    val cardNumbers: List<List<String>>
)

class PreferencesManager(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val LAST_TOOL_KEY = stringPreferencesKey("last_tool")
        private val PENDING_SETUP_GAME_IDS_KEY = stringPreferencesKey("pending_setup_game_ids")
        private const val CARD_SETUP_STATE_PREFIX = "card_setup_state_"
    }

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getLastTool(): ToolType? {
        return dataStore.data.map { preferences ->
            preferences[LAST_TOOL_KEY]?.let { value ->
                try {
                    ToolType.valueOf(value)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }.firstOrNull()
    }

    suspend fun setLastTool(tool: ToolType) {
        dataStore.edit { preferences ->
            preferences[LAST_TOOL_KEY] = tool.name
        }
    }

    suspend fun getPendingSetupGameIds(): List<Long> {
        return dataStore.data.map { preferences ->
            preferences[PENDING_SETUP_GAME_IDS_KEY]
                ?.split(",")
                ?.mapNotNull { it.toLongOrNull() }
                ?: emptyList()
        }.firstOrNull() ?: emptyList()
    }

    suspend fun addPendingSetupGameId(gameId: Long) {
        dataStore.edit { preferences ->
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

    suspend fun removePendingSetupGameId(gameId: Long) {
        dataStore.edit { preferences ->
            val current = preferences[PENDING_SETUP_GAME_IDS_KEY]
                ?.split(",")
                ?.mapNotNull { it.toLongOrNull() }
                ?.toMutableList()
                ?: mutableListOf()
            current.remove(gameId)
            preferences[PENDING_SETUP_GAME_IDS_KEY] = current.joinToString(",")
        }
    }

    suspend fun clearPendingSetupGameIds() {
        dataStore.edit { preferences ->
            preferences.remove(PENDING_SETUP_GAME_IDS_KEY)
        }
    }

    suspend fun getCardSetupState(gameId: Long): CardSetupState? {
        val key = stringPreferencesKey("$CARD_SETUP_STATE_PREFIX$gameId")
        return dataStore.data.map { preferences ->
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

    suspend fun setCardSetupState(gameId: Long, state: CardSetupState) {
        val key = stringPreferencesKey("$CARD_SETUP_STATE_PREFIX$gameId")
        val data = CardSetupStateData(state.currentColumn, state.cardNumbers)
        dataStore.edit { preferences ->
            preferences[key] = json.encodeToString(data)
        }
    }

    suspend fun clearCardSetupState(gameId: Long) {
        val key = stringPreferencesKey("$CARD_SETUP_STATE_PREFIX$gameId")
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}

data class CardSetupState(
    val currentColumn: Int,
    val cardNumbers: List<List<String>>
)

enum class ToolType {
    BINGO,
    SUDOKU
}

expect fun createDataStore(context: Any? = null): DataStore<Preferences>

internal const val DATA_STORE_FILE_NAME = "flexapp.preferences_pb"
