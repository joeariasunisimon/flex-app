package co.jarias.flexapp.ui.screens.bingo.game_setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.data.repository.GameRepository
import co.jarias.flexapp.domain.usecase.CreateGameUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BingoGameSetupScreenViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BingoGameSetupScreenState())
    val state: StateFlow<BingoGameSetupScreenState> = _state.asStateFlow()

    private val createGameUseCase = CreateGameUseCase(gameRepository)

    fun onEvent(event: BingoGameSetupScreenEvents) {
        when (event) {
            is BingoGameSetupScreenEvents.OnGameNameChanged -> {
                _state.value = _state.value.copy(
                    gameName = event.name,
                    errorMessage = null
                )
            }
            is BingoGameSetupScreenEvents.OnCreateGameClicked -> createGame()
        }
    }

    private fun createGame() {
        val currentState = _state.value

        if (currentState.gameName.isBlank()) {
            _state.value = currentState.copy(errorMessage = "Please enter a game name")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = currentState.copy(isLoading = true, errorMessage = null)

                val game = createGameUseCase(currentState.gameName)

                game.id?.let { gameId ->
                    _state.value = currentState.copy(
                        isLoading = false,
                        gameCreated = true,
                        createdGameId = gameId,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Failed to create game: ${e.message}"
                )
            }
        }
    }
}
