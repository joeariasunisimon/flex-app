package co.jarias.flexapp.ui.screens.bingo.game_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.data.local.PreferencesManager
import co.jarias.flexapp.data.repository.BingoCardRepository
import co.jarias.flexapp.data.repository.GameRepository
import co.jarias.flexapp.data.repository.MarkedNumberRepository
import co.jarias.flexapp.domain.usecase.DropGameUseCase
import co.jarias.flexapp.domain.usecase.GetAllGamesUseCase
import co.jarias.flexapp.domain.usecase.RestartGameUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BingoGameListScreenViewModel(
    private val gameRepository: GameRepository,
    private val bingoCardRepository: BingoCardRepository,
    private val markedNumberRepository: MarkedNumberRepository,
    private val preferencesManager: PreferencesManager? = null
) : ViewModel() {

    private val _state = MutableStateFlow(BingoGameListScreenState())
    val state: StateFlow<BingoGameListScreenState> = _state.asStateFlow()

    private val getAllGamesUseCase = GetAllGamesUseCase(gameRepository)
    private val restartGameUseCase = RestartGameUseCase(gameRepository, markedNumberRepository)
    private val dropGameUseCase = DropGameUseCase(gameRepository, bingoCardRepository, markedNumberRepository)

    init {
        loadGames()
    }

    fun onEvent(event: BingoGameListScreenEvents) {
        when (event) {
            is BingoGameListScreenEvents.OnCreateNewGameClicked -> {}
            is BingoGameListScreenEvents.OnPlayGame -> {}
            is BingoGameListScreenEvents.OnRestartGame -> {
                restartGame(event.gameId)
            }
            is BingoGameListScreenEvents.OnDeleteGame -> {
                deleteGame(event.gameId)
            }
            is BingoGameListScreenEvents.OnRetryClicked -> {
                loadGames()
            }
            is BingoGameListScreenEvents.OnContinueSetup -> {
                continueSetup(event.gameId)
            }
        }
    }

    private fun loadGames() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)

                val games = getAllGamesUseCase()
                val pendingIds = preferencesManager?.getPendingSetupGameIds() ?: emptyList()

                _state.value = _state.value.copy(
                    games = games,
                    isLoading = false,
                    pendingSetupGameIds = pendingIds
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load games: ${e.message}"
                )
            }
        }
    }

    private fun continueSetup(gameId: Long) {
        viewModelScope.launch {
            try {
                val cards = withContext(Dispatchers.IO) {
                    bingoCardRepository.getCardsByGameId(gameId)
                }

                if (cards.isEmpty()) {
                    _state.value = _state.value.copy(continueToCardSetup = gameId)
                } else {
                    val game = withContext(Dispatchers.IO) {
                        gameRepository.getGameById(gameId)
                    }
                    if (game?.targetFigure == null) {
                        _state.value = _state.value.copy(continueToFigureSelection = gameId)
                    } else {
                        preferencesManager?.removePendingSetupGameId(gameId)
                        _state.value = _state.value.copy(continueToGamePlay = gameId)
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = "Failed to continue setup: ${e.message}")
            }
        }
    }

    private fun restartGame(gameId: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    restartGameUseCase(gameId)
                }
                preferencesManager?.removePendingSetupGameId(gameId)
                loadGames()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Failed to restart game: ${e.message}"
                )
            }
        }
    }

    private fun deleteGame(gameId: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dropGameUseCase(gameId)
                }
                preferencesManager?.removePendingSetupGameId(gameId)
                loadGames()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Failed to delete game: ${e.message}"
                )
            }
        }
    }

    fun clearNavigationState() {
        _state.value = _state.value.copy(
            continueToCardSetup = null,
            continueToFigureSelection = null,
            continueToGamePlay = null
        )
    }
}