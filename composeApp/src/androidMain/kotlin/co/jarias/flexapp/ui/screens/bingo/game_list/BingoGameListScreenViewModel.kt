package co.jarias.flexapp.ui.screens.bingo.game_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.data.local.PreferencesManager
import co.jarias.flexapp.data.repository.BingoCardRepository
import co.jarias.flexapp.data.repository.GameRepository
import co.jarias.flexapp.domain.usecase.DropGameUseCase
import co.jarias.flexapp.domain.usecase.GetAllGamesUseCase
import co.jarias.flexapp.domain.usecase.RestartGameUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BingoGameListScreenViewModel(
    private val getAllGamesUseCase: GetAllGamesUseCase,
    private val restartGameUseCase: RestartGameUseCase,
    private val dropGameUseCase: DropGameUseCase,
    private val bingoCardRepository: BingoCardRepository,
    private val gameRepository: GameRepository,
    private val preferencesManager: PreferencesManager? = null
) : ViewModel() {

    private val _state = MutableStateFlow(BingoGameListScreenState())
    val state: StateFlow<BingoGameListScreenState> = _state.asStateFlow()

    init {
        observeGames()
        refreshPendingIds()
    }

    private fun observeGames() {
        getAllGamesUseCase()
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { games ->
                _state.update { it.copy(games = games, isLoading = false, errorMessage = null) }
            }
            .catch { e ->
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to load games: ${e.message}") }
            }
            .launchIn(viewModelScope)
    }

    private fun refreshPendingIds() {
        viewModelScope.launch {
            val pendingIds = preferencesManager?.getPendingSetupGameIds() ?: emptyList()
            _state.update { it.copy(pendingSetupGameIds = pendingIds) }
        }
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
                observeGames()
                refreshPendingIds()
            }
            is BingoGameListScreenEvents.OnContinueSetup -> {
                continueSetup(event.gameId)
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
                refreshPendingIds()
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
                refreshPendingIds()
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