package co.jarias.flexapp.ui.screens.bingo.game_play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.domain.usecase.CompleteGameUseCase
import co.jarias.flexapp.domain.usecase.GetGameStateUseCase
import co.jarias.flexapp.domain.usecase.MarkNumberUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BingoGamePlayScreenViewModel(
    private val getGameStateUseCase: GetGameStateUseCase,
    private val markNumberUseCase: MarkNumberUseCase,
    private val completeGameUseCase: CompleteGameUseCase,
    private val gameId: Long = 0
) : ViewModel() {

    private val _state = MutableStateFlow(BingoGamePlayScreenState())
    val state: StateFlow<BingoGamePlayScreenState> = _state.asStateFlow()

    private var currentGameId: Long = gameId

    init {
        if (gameId != 0L) {
            loadGame(gameId)
        }
    }

    fun loadGame(gameId: Long) {
        currentGameId = gameId
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)

                val gameState = getGameStateUseCase(gameId)

                if (gameState != null) {
                    _state.value = _state.value.copy(
                        gameState = gameState,
                        isLoading = false,
                        showWinDialog = gameState.isWon
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Game not found"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load game: ${e.message}"
                )
            }
        }
    }

    fun onEvent(event: BingoGamePlayScreenEvents) {
        when (event) {
            is BingoGamePlayScreenEvents.OnNumberMarked -> markNumber(event.number)
            is BingoGamePlayScreenEvents.OnWinDialogDismissed -> dismissWinDialog()
            is BingoGamePlayScreenEvents.OnRetryClicked -> {
                loadGame(currentGameId)
            }
        }
    }

    private fun markNumber(number: Int) {
        val currentState = _state.value
        val gameState = currentState.gameState
        val gameId = gameState?.game?.id ?: return

        viewModelScope.launch {
            try {
                val success = markNumberUseCase(gameId, number)

                if (success) {
                    val updatedGameState = getGameStateUseCase(gameId)

                    _state.value = currentState.copy(
                        gameState = updatedGameState,
                        lastMarkedNumber = number,
                        showWinDialog = updatedGameState?.isWon == true
                    )

                    if (updatedGameState?.isWon == true) {
                        completeGameUseCase(gameId)
                    }
                }
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    errorMessage = "Failed to mark number: ${e.message}"
                )
            }
        }
    }

    private fun dismissWinDialog() {
        _state.value = _state.value.copy(showWinDialog = false)
    }
}
