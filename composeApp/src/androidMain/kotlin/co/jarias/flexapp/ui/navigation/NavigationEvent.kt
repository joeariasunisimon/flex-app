package co.jarias.flexapp.ui.navigation

import co.jarias.flexapp.data.local.ToolType

sealed class NavigationEvent {
    data object OnNavigateUp : NavigationEvent()
    data object NavigateToToolSelection : NavigationEvent()
    data object NavigateToBingoGameList : NavigationEvent()
    data object NavigateToBingoGameSetup : NavigationEvent()
    data class NavigateToBingoCardSetup(val gameId: Long) : NavigationEvent()
    data class NavigateToBingoCardScanner(val gameId: Long) : NavigationEvent()
    data class NavigateToBingoFigureSelection(val gameId: Long) : NavigationEvent()
    data class NavigateToBingoGamePlay(val gameId: Long) : NavigationEvent()
    data class NavigateToTool(val toolType: ToolType) : NavigationEvent()
}

fun handleNavigationEvent(
    navController: androidx.navigation.NavHostController,
    event: NavigationEvent
) {
    when (event) {
        is NavigationEvent.OnNavigateUp -> navController.popBackStack()

        is NavigationEvent.NavigateToToolSelection -> {
            navController.navigate(AppDestinations.TOOL_SELECTION) {
                popUpTo(AppDestinations.WELCOME) { inclusive = true }
            }
        }

        is NavigationEvent.NavigateToBingoGameList -> {
            navController.navigate(AppDestinations.BINGO_GAME_LIST) {
                popUpTo(AppDestinations.TOOL_SELECTION)
            }
        }

        is NavigationEvent.NavigateToBingoGameSetup -> {
            navController.navigate(AppDestinations.BINGO_GAME_SETUP)
        }

        is NavigationEvent.NavigateToBingoCardSetup -> {
            navController.navigate(AppDestinations.bingoCardSetupRoute(event.gameId)) {
                popUpTo(AppDestinations.BINGO_GAME_LIST)
            }
        }

        is NavigationEvent.NavigateToBingoCardScanner -> {
            navController.navigate(AppDestinations.bingoCardScannerRoute(event.gameId))
        }

        is NavigationEvent.NavigateToBingoFigureSelection -> {
            navController.navigate(AppDestinations.bingoFigureSelectionRoute(event.gameId)) {
                popUpTo(AppDestinations.BINGO_GAME_LIST)
            }
        }

        is NavigationEvent.NavigateToBingoGamePlay -> {
            navController.navigate(AppDestinations.bingoGamePlayRoute(event.gameId)) {
                popUpTo(AppDestinations.BINGO_GAME_LIST)
            }
        }

        is NavigationEvent.NavigateToTool -> {
            navController.navigate(AppDestinations.TOOL_SELECTION) {
                popUpTo(AppDestinations.WELCOME) { inclusive = true }
            }
            if (event.toolType == ToolType.BINGO) {
                navController.navigate(AppDestinations.BINGO_GAME_LIST)
            }
        }
    }
}
