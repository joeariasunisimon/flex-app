package co.jarias.flexapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.data.repository.BingoCardRepositoryImpl
import co.jarias.flexapp.data.repository.GameRepositoryImpl
import co.jarias.flexapp.data.repository.MarkedNumberRepositoryImpl
import co.jarias.flexapp.domain.GameState
import co.jarias.flexapp.domain.usecase.CheckWinConditionUseCase
import co.jarias.flexapp.domain.usecase.CompleteGameUseCase
import co.jarias.flexapp.domain.usecase.GetGameStateUseCase
import co.jarias.flexapp.domain.usecase.MarkNumberUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BingoGamePlayUiState(
    val gameState: GameState? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showWinDialog: Boolean = false,
    val lastMarkedNumber: Int? = null
)

class BingoGamePlayViewModel(
    private val gameRepository: GameRepositoryImpl,
    private val bingoCardRepository: BingoCardRepositoryImpl,
    private val markedNumberRepository: MarkedNumberRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(BingoGamePlayUiState())
    val uiState: StateFlow<BingoGamePlayUiState> = _uiState

    private val getGameStateUseCase = GetGameStateUseCase(
        gameRepository, bingoCardRepository, markedNumberRepository,
        CheckWinConditionUseCase(bingoCardRepository, markedNumberRepository)
    )
    private val markNumberUseCase = MarkNumberUseCase(markedNumberRepository, bingoCardRepository)
    private val completeGameUseCase = CompleteGameUseCase(gameRepository, markedNumberRepository)

    fun loadGame(gameId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val gameState = getGameStateUseCase(gameId)

                if (gameState != null) {
                    _uiState.value = _uiState.value.copy(
                        gameState = gameState,
                        isLoading = false,
                        showWinDialog = gameState.isWon
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Game not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load game: ${e.message}"
                )
            }
        }
    }

    fun markNumber(number: Int) {
        val currentState = _uiState.value
        val gameState = currentState.gameState ?: return
        val gameId = gameState.game.id ?: return

        viewModelScope.launch {
            try {
                val success = markNumberUseCase(gameId, number)

                if (success) {
                    // Reload game state to get updated marked numbers and win status
                    val updatedGameState = getGameStateUseCase(gameId)

                    _uiState.value = currentState.copy(
                        gameState = updatedGameState,
                        lastMarkedNumber = number,
                        showWinDialog = updatedGameState?.isWon == true
                    )

                    // If won, complete the game
                    if (updatedGameState?.isWon == true) {
                        completeGameUseCase(gameId)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    errorMessage = "Failed to mark number: ${e.message}"
                )
            }
        }
    }

    fun dismissWinDialog() {
        _uiState.value = _uiState.value.copy(showWinDialog = false)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
