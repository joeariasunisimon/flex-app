package co.jarias.flexapp.ui.screens.bingo.game_setup

sealed class BingoGameSetupScreenEvents {
    data class OnGameNameChanged(val name: String) : BingoGameSetupScreenEvents()
    data object OnCreateGameClicked : BingoGameSetupScreenEvents()
}
