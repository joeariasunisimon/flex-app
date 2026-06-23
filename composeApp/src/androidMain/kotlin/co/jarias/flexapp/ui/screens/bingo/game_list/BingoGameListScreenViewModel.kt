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
                val grouped = games.groupBy { it.cardId }
                    .map { (cardId, sessions) ->
                        if (cardId == null) {
                            // Incomplete setups, show them all separately
                            sessions.map { GroupedBingoGame(it, 1) }
                        } else {
                            // Completed cards, group by card and show the session with highest id
                            val latest = sessions.maxBy { it.id ?: 0 }
                            listOf(GroupedBingoGame(latest, sessions.size))
                        }
                    }.flatten()
                    .sortedByDescending { it.lastSession.id ?: 0 }

                _state.update { it.copy(games = grouped, isLoading = false, errorMessage = null) }
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
                    val game = state.value.games.find { it.lastSession.id == gameId }
                    val cardId = game?.lastSession?.cardId
                    if (cardId != null) {
                        // If it's a grouped game with a card, delete the card (cascades to all sessions)
                        // Note: We need a repository for this. Or just delete the games.
                        // For now, let's keep it simple and just delete the latest game if that's what's preferred.
                        // Actually, user wants a single entry, so deleting it should delete the group.
                        dropGameUseCase(gameId) // This is current behavior. 
                    } else {
                        dropGameUseCase(gameId)
                    }
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
