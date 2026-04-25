package co.jarias.flexapp.ui.screens.bingo.figure_selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.data.local.PreferencesManager
import co.jarias.flexapp.data.repository.BingoCardRepository
import co.jarias.flexapp.data.repository.GameRepository
import co.jarias.flexapp.domain.WinCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BingoFigureSelectionScreenViewModel(
    private val bingoCardRepository: BingoCardRepository,
    private val gameRepository: GameRepository,
    private val preferencesManager: PreferencesManager? = null,
    private val gameId: Long = 0
) : ViewModel() {

    private val _state = MutableStateFlow(BingoFigureSelectionScreenState())
    val state: StateFlow<BingoFigureSelectionScreenState> = _state.asStateFlow()

    init {
        if (gameId != 0L) {
            loadGame(gameId)
        }
    }

    fun loadGame(gameId: Long) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, gameId = gameId)

                val cards = withContext(Dispatchers.IO) {
                    bingoCardRepository.getCardsByGameId(gameId)
                }

                if (cards.isNotEmpty()) {
                    _state.value = _state.value.copy(
                        cardGrid = cards.first().grid,
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "No card found"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load card: ${e.message}"
                )
            }
        }
    }

    fun onEvent(event: BingoFigureSelectionScreenEvents) {
        when (event) {
            is BingoFigureSelectionScreenEvents.OnFigureSelected -> {
                _state.value = _state.value.copy(
                    selectedFigure = event.figure,
                    customPattern = emptySet()
                )
            }
            is BingoFigureSelectionScreenEvents.OnCustomPatternToggled -> {
                val pos = Pair(event.row, event.col)
                val newPattern = if (_state.value.customPattern.contains(pos)) {
                    _state.value.customPattern - pos
                } else {
                    _state.value.customPattern + pos
                }
                _state.value = _state.value.copy(
                    customPattern = newPattern,
                    selectedFigure = null
                )
            }
            is BingoFigureSelectionScreenEvents.OnContinue -> {
                saveFigure()
            }
        }
    }

    private fun saveFigure() {
        val currentState = _state.value

        if (currentState.selectedFigure == null && currentState.customPattern.isEmpty()) {
            _state.value = currentState.copy(
                errorMessage = "Please select a figure or create a custom pattern"
            )
            return
        }

        viewModelScope.launch {
            try {
                _state.value = currentState.copy(isUpdating = true)

                val winCondition = currentState.selectedFigure
                    ?: WinCondition.Custom(requiredCells = currentState.customPattern)

                val game = withContext(Dispatchers.IO) {
                    gameRepository.getGameById(currentState.gameId)
                }

                if (game != null) {
                    val updatedGame = game.copy(targetFigure = winCondition)
                    withContext(Dispatchers.IO) {
                        gameRepository.updateGame(updatedGame)
                    }
                    preferencesManager?.removePendingSetupGameId(currentState.gameId)
                }

                _state.value = currentState.copy(
                    isUpdating = false,
                    gameReady = true
                )
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isUpdating = false,
                    errorMessage = "Failed to save figure: ${e.message}"
                )
            }
        }
    }
}
