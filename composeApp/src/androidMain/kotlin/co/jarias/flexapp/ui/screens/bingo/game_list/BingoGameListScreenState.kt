package co.jarias.flexapp.ui.screens.bingo.game_list

import co.jarias.flexapp.domain.Game

data class BingoGameListScreenState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
