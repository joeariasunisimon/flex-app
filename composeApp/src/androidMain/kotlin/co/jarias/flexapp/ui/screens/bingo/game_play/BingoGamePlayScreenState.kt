package co.jarias.flexapp.ui.screens.bingo.game_play

import co.jarias.flexapp.domain.GameState

data class BingoGamePlayScreenState(
    val gameState: GameState? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showWinDialog: Boolean = false,
    val lastMarkedNumber: Int? = null
)
