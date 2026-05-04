package co.jarias.flexapp.ui.screens.bingo.game_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BingoGameListScreenViewModel(
    private val getAllGamesUseCase: GetAllGamesUseCase,
    private val restartGameUseCase: RestartGameUseCase,
    private val dropGameUseCase: DropGameUseCase,
    private val checkGameSetupStatusUseCase: CheckGameSetupStatusUseCase,
    private val getPendingSetupGameIdsUseCase: GetPendingSetupGameIdsUseCase,
    private val removePendingSetupGameIdUseCase: RemovePendingSetupGameIdUseCase
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
            val pendingIds = getPendingSetupGameIdsUseCase()
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
                when (checkGameSetupStatusUseCase(gameId)) {
                    GameSetupStatus.CardSetupRequired -> {
                        _state.value = _state.value.copy(continueToCardSetup = gameId)
                    }
                    GameSetupStatus.FigureSelectionRequired -> {
                        _state.value = _state.value.copy(continueToFigureSelection = gameId)
                    }
                    GameSetupStatus.SetupComplete -> {
                        _state.value = _state.value.copy(continueToGamePlay = gameId)
                        refreshPendingIds()
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
                removePendingSetupGameIdUseCase(gameId)
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
                removePendingSetupGameIdUseCase(gameId)
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
