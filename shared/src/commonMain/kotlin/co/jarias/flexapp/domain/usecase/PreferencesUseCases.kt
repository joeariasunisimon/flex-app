package co.jarias.flexapp.domain.usecase

import co.jarias.flexapp.data.local.CardSetupState
import co.jarias.flexapp.data.local.PreferencesManager
import co.jarias.flexapp.data.local.ToolType

class GetLastToolUseCase(private val preferencesManager: PreferencesManager) {
    suspend operator fun invoke(): ToolType? = preferencesManager.getLastTool()
}

class SetLastToolUseCase(private val preferencesManager: PreferencesManager) {
    suspend operator fun invoke(tool: ToolType) = preferencesManager.setLastTool(tool)
}

class GetPendingSetupGameIdsUseCase(private val preferencesManager: PreferencesManager) {
    suspend operator fun invoke(): List<Long> = preferencesManager.getPendingSetupGameIds()
}

class AddPendingSetupGameIdUseCase(private val preferencesManager: PreferencesManager) {
    suspend operator fun invoke(gameId: Long) = preferencesManager.addPendingSetupGameId(gameId)
}

class RemovePendingSetupGameIdUseCase(private val preferencesManager: PreferencesManager) {
    suspend operator fun invoke(gameId: Long) = preferencesManager.removePendingSetupGameId(gameId)
}

class ClearPendingSetupGameIdsUseCase(private val preferencesManager: PreferencesManager) {
    suspend operator fun invoke() = preferencesManager.clearPendingSetupGameIds()
}

class GetCardSetupStateUseCase(private val preferencesManager: PreferencesManager) {
    suspend operator fun invoke(gameId: Long): CardSetupState? = preferencesManager.getCardSetupState(gameId)
}

class SaveCardSetupStateUseCase(private val preferencesManager: PreferencesManager) {
    suspend operator fun invoke(gameId: Long, state: CardSetupState) = preferencesManager.setCardSetupState(gameId, state)
}

class ClearCardSetupStateUseCase(private val preferencesManager: PreferencesManager) {
    suspend operator fun invoke(gameId: Long) = preferencesManager.clearCardSetupState(gameId)
}
