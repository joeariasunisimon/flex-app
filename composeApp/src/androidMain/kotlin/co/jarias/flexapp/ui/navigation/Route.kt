package co.jarias.flexapp.ui.navigation

sealed class Route(val route: String) {
    data object Welcome : Route("welcome")
    data object ToolSelection : Route("tool_selection")
    data object BingoGameSetup : Route("bingo_game_setup")
    data object BingoGameList : Route("bingo_game_list")
    data object BingoCardSetup : Route("bingo_card_setup/{gameId}") {
        fun createRoute(gameId: Long) = "bingo_card_setup/$gameId"
    }
    data object BingoFigureSelection : Route("bingo_figure_selection/{gameId}") {
        fun createRoute(gameId: Long) = "bingo_figure_selection/$gameId"
    }
    data object BingoGamePlay : Route("bingo_game_play/{gameId}") {
        fun createRoute(gameId: Long) = "bingo_game_play/$gameId"
    }
}
