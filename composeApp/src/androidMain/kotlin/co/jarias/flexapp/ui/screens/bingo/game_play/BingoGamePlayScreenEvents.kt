package co.jarias.flexapp.ui.screens.bingo.game_play

sealed class BingoGamePlayScreenEvents {
    data class OnNumberMarked(val number: Int) : BingoGamePlayScreenEvents()
    data object OnWinDialogDismissed : BingoGamePlayScreenEvents()
    data object OnBackToMenuClicked : BingoGamePlayScreenEvents()
    data object OnBackPressed : BingoGamePlayScreenEvents()
    data object OnRetryClicked : BingoGamePlayScreenEvents()
}
