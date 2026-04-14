package co.jarias.flexapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.data.repository.GameRepositoryImpl
import co.jarias.flexapp.domain.Game
import co.jarias.flexapp.domain.usecase.GetAllGamesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BingoGameListUiState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class BingoGameListViewModel(
    private val gameRepository: GameRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(BingoGameListUiState())
    val uiState: StateFlow<BingoGameListUiState> = _uiState

    private val getAllGamesUseCase = GetAllGamesUseCase(gameRepository)

    init {
        loadGames()
    }

    fun loadGames() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val games = getAllGamesUseCase()

                _uiState.value = _uiState.value.copy(
                    games = games,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load games: ${e.message}"
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
