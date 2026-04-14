package co.jarias.flexapp.ui.screens.bingo.game_list

sealed class BingoGameListScreenEvents {
    data object OnCreateNewGameClicked : BingoGameListScreenEvents()
    data class OnPlayGame(val gameId: Long) : BingoGameListScreenEvents()
    data class OnRestartGame(val gameId: Long) : BingoGameListScreenEvents()
    data class OnDeleteGame(val gameId: Long) : BingoGameListScreenEvents()
    data object OnRetryClicked : BingoGameListScreenEvents()
}
