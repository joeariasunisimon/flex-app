package co.jarias.flexapp.ui.screens.bingo.game_setup

data class BingoGameSetupScreenState(
    val gameName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val gameCreated: Boolean = false,
    val createdGameId: Long? = null
)
