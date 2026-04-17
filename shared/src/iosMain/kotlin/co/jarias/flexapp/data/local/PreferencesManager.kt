package co.jarias.flexapp.data.local

import platform.Foundation.NSUserDefaults

actual class PreferencesManager {
    companion object {
        private const val LAST_TOOL_KEY = "last_tool"
        private const val PENDING_SETUP_GAME_IDS_KEY = "pending_setup_game_ids"
        private const val CARD_SETUP_STATE_PREFIX = "card_setup_state_"
    }

    actual suspend fun getLastTool(): ToolType? {
        val value = NSUserDefaults.standardUserDefaults.stringForKey(LAST_TOOL_KEY)
        return value?.let {
            try {
                ToolType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    actual suspend fun setLastTool(tool: ToolType) {
        NSUserDefaults.standardUserDefaults.setObject(tool.name, forKey = LAST_TOOL_KEY)
    }

    actual suspend fun getPendingSetupGameIds(): List<Long> {
        val value = NSUserDefaults.standardUserDefaults.stringForKey(PENDING_SETUP_GAME_IDS_KEY)
        return value?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
    }

    actual suspend fun addPendingSetupGameId(gameId: Long) {
        val current = getPendingSetupGameIds().toMutableList()
        if (!current.contains(gameId)) {
            current.add(gameId)
            NSUserDefaults.standardUserDefaults.setObject(current.joinToString(","), forKey = PENDING_SETUP_GAME_IDS_KEY)
        }
    }

    actual suspend fun removePendingSetupGameId(gameId: Long) {
        val current = getPendingSetupGameIds().toMutableList()
        current.remove(gameId)
        NSUserDefaults.standardUserDefaults.setObject(current.joinToString(","), forKey = PENDING_SETUP_GAME_IDS_KEY)
    }

    actual suspend fun clearPendingSetupGameIds() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(PENDING_SETUP_GAME_IDS_KEY)
    }

    actual suspend fun getCardSetupState(gameId: Long): CardSetupState? {
        val key = "$CARD_SETUP_STATE_PREFIX$gameId"
        val value = NSUserDefaults.standardUserDefaults.stringForKey(key) ?: return null
        return try {
            val parts = value.split("|")
            if (parts.size != 2) null
            else {
                val currentColumn = parts[0].toIntOrNull() ?: return null
                val cardNumbers = parts[1].split(";").map { row ->
                    if (row.isEmpty()) List(5) { "" }
                    else row.split(",").map { it }
                }
                CardSetupState(currentColumn, cardNumbers)
            }
        } catch (e: Exception) {
            null
        }
    }

    actual suspend fun setCardSetupState(gameId: Long, state: CardSetupState) {
        val key = "$CARD_SETUP_STATE_PREFIX$gameId"
        val cardNumbersStr = state.cardNumbers.joinToString(";") { row ->
            row.joinToString(",")
        }
        val value = "${state.currentColumn}|$cardNumbersStr"
        NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)
    }

    actual suspend fun clearCardSetupState(gameId: Long) {
        val key = "$CARD_SETUP_STATE_PREFIX$gameId"
        NSUserDefaults.standardUserDefaults.removeObjectForKey(key)
    }
}