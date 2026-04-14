package co.jarias.flexapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.data.repository.GameRepositoryImpl
import co.jarias.flexapp.domain.usecase.CreateGameUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BingoGameSetupUiState(
    val gameName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val gameCreated: Boolean = false,
    val createdGameId: Long? = null
)

class BingoGameViewModel(
    private val gameRepository: GameRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(BingoGameSetupUiState())
    val uiState: StateFlow<BingoGameSetupUiState> = _uiState

    private val createGameUseCase = CreateGameUseCase(gameRepository)

    fun updateGameName(name: String) {
        _uiState.value = _uiState.value.copy(gameName = name)
    }

    fun createGame() {
        val state = _uiState.value

        if (state.gameName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter a game name")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = state.copy(isLoading = true, errorMessage = null)

                // Create the game
                val game = createGameUseCase(state.gameName)

                game.id?.let { gameId ->
                    _uiState.value = state.copy(
                        isLoading = false,
                        gameCreated = true,
                        createdGameId = gameId,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Failed to create game: ${e.message}"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = BingoGameSetupUiState()
    }
}
