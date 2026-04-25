package co.jarias.flexapp.ui.navigation

object AppDestinations {
    const val WELCOME = "welcome"
    const val TOOL_SELECTION = "tool_selection"
    const val BINGO_GAME_SETUP = "bingo_game_setup"
    const val BINGO_GAME_LIST = "bingo_game_list"

    private const val BINGO_CARD_SETUP = "bingo_card_setup"
    const val BINGO_CARD_SETUP_ROUTE = "$BINGO_CARD_SETUP/{${NavArguments.GAME_ID}}"
    fun bingoCardSetupRoute(gameId: Long) = "$BINGO_CARD_SETUP/$gameId"

    private const val BINGO_CARD_SCANNER = "bingo_card_scanner"
    const val BINGO_CARD_SCANNER_ROUTE = "$BINGO_CARD_SCANNER/{${NavArguments.GAME_ID}}"
    fun bingoCardScannerRoute(gameId: Long) = "$BINGO_CARD_SCANNER/$gameId"

    private const val BINGO_FIGURE_SELECTION = "bingo_figure_selection"
    const val BINGO_FIGURE_SELECTION_ROUTE = "$BINGO_FIGURE_SELECTION/{${NavArguments.GAME_ID}}"
    fun bingoFigureSelectionRoute(gameId: Long) = "$BINGO_FIGURE_SELECTION/$gameId"

    private const val BINGO_GAME_PLAY = "bingo_game_play"
    const val BINGO_GAME_PLAY_ROUTE = "$BINGO_GAME_PLAY/{${NavArguments.GAME_ID}}"
    fun bingoGamePlayRoute(gameId: Long) = "$BINGO_GAME_PLAY/$gameId"
}

object NavArguments {
    const val GAME_ID = "gameId"
}
