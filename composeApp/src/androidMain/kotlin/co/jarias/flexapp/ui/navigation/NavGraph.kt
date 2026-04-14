package co.jarias.flexapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.jarias.flexapp.ui.screens.welcome.WelcomeScreen
import co.jarias.flexapp.ui.screens.tools.ToolSelectionScreen
import co.jarias.flexapp.ui.screens.bingo.BingoGameSetupScreen
import co.jarias.flexapp.ui.screens.bingo.BingoGamePlayScreen
import co.jarias.flexapp.ui.screens.bingo.BingoGameListScreen
import co.jarias.flexapp.ui.screens.bingo.BingoCardSetupScreen
import co.jarias.flexapp.ui.screens.bingo.BingoFigureSelectionScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.Welcome.route
    ) {
        composable(Route.Welcome.route) {
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate(Route.ToolSelection.route) {
                        popUpTo(Route.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.ToolSelection.route) {
            ToolSelectionScreen(
                onBingoSelected = {
                    navController.navigate(Route.BingoGameList.route)
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        composable(Route.BingoGameList.route) {
            BingoGameListScreen(
                onGamePlay = { gameId ->
                    navController.navigate(Route.BingoGamePlay.createRoute(gameId))
                },
                onGameSetup = { gameId ->
                    navController.navigate(Route.BingoCardSetup.createRoute(gameId))
                },
                onCreateNewGame = {
                    navController.navigate(Route.BingoGameSetup.route)
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        composable(Route.BingoGameSetup.route) {
            BingoGameSetupScreen(
                onGameCreated = { gameId ->
                    navController.navigate(Route.BingoCardSetup.createRoute(gameId)) {
                        popUpTo(Route.BingoGameList.route)
                    }
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        composable(Route.BingoGamePlay.route) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toLongOrNull()
            if (gameId != null) {
                BingoGamePlayScreen(
                    gameId = gameId,
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Route.BingoCardSetup.route) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toLongOrNull()
            if (gameId != null) {
                BingoCardSetupScreen(
                    gameId = gameId,
                    onCardSaved = {
                        navController.navigate(Route.BingoFigureSelection.createRoute(gameId)) {
                            popUpTo(Route.BingoGameList.route)
                        }
                    },
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Route.BingoFigureSelection.route) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toLongOrNull()
            if (gameId != null) {
                BingoFigureSelectionScreen(
                    gameId = gameId,
                    onFigureSelected = { figure ->
                        // Update game with figure
                        // Then navigate to play
                        navController.navigate(Route.BingoGamePlay.createRoute(gameId)) {
                            popUpTo(Route.BingoGameList.route)
                        }
                    },
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
