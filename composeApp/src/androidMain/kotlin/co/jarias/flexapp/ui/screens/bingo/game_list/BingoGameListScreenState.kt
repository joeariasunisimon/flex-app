package co.jarias.flexapp.ui.screens.bingo.game_list

import co.jarias.flexapp.domain.Game

data class GroupedBingoGame(
    val lastSession: Game,
    val totalPlayed: Int
)

data class BingoGameListScreenState(
    val games: List<GroupedBingoGame> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val pendingSetupGameIds: List<Long> = emptyList(),
    val continueToCardSetup: Long? = null,
    val continueToFigureSelection: Long? = null,
    val continueToGamePlay: Long? = null
)
