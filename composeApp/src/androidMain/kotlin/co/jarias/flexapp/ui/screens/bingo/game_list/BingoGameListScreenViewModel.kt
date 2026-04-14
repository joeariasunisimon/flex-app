package co.jarias.flexapp.ui.screens.bingo.game_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.data.repository.BingoCardRepository
import co.jarias.flexapp.data.repository.GameRepository
import co.jarias.flexapp.data.repository.MarkedNumberRepository
import co.jarias.flexapp.domain.usecase.DropGameUseCase
import co.jarias.flexapp.domain.usecase.GetAllGamesUseCase
import co.jarias.flexapp.domain.usecase.RestartGameUseCase
import co.jarias.flexapp.ui.navigation.NavigationEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BingoGameListScreenViewModel(
    private val gameRepository: GameRepository,
    private val bingoCardRepository: BingoCardRepository,
    private val markedNumberRepository: MarkedNumberRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BingoGameListScreenState())
    val state: StateFlow<BingoGameListScreenState> = _state.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    private val getAllGamesUseCase = GetAllGamesUseCase(gameRepository)
    private val restartGameUseCase = RestartGameUseCase(gameRepository, markedNumberRepository)
    private val dropGameUseCase = DropGameUseCase(gameRepository, bingoCardRepository, markedNumberRepository)

    init {
        loadGames()
    }

    fun onEvent(event: BingoGameListScreenEvents) {
        when (event) {
            is BingoGameListScreenEvents.OnCreateNewGameClicked -> {
                _navigationEvent.value = NavigationEvent.NavigateToBingoGameSetup
            }
            is BingoGameListScreenEvents.OnBackPressed -> {
                _navigationEvent.value = NavigationEvent.OnNavigateUp
            }
            is BingoGameListScreenEvents.OnPlayGame -> {
                _navigationEvent.value = NavigationEvent.NavigateToBingoGamePlay(event.gameId)
            }
            is BingoGameListScreenEvents.OnRestartGame -> {
                restartGame(event.gameId)
            }
            is BingoGameListScreenEvents.OnDeleteGame -> {
                deleteGame(event.gameId)
            }
            is BingoGameListScreenEvents.OnRetryClicked -> {
                loadGames()
            }
        }
    }

    private fun loadGames() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)

                val games = getAllGamesUseCase()

                _state.value = _state.value.copy(
                    games = games,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load games: ${e.message}"
                )
            }
        }
    }

    private fun restartGame(gameId: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    restartGameUseCase(gameId)
                }
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
                loadGames()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Failed to delete game: ${e.message}"
                )
            }
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}
