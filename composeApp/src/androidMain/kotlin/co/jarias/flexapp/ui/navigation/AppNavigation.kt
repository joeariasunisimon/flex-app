package co.jarias.flexapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import co.jarias.flexapp.ui.screens.bingo.card_setup.BingoCardSetupScreen
import co.jarias.flexapp.ui.screens.bingo.card_setup.BingoCardSetupScreenViewModel
import co.jarias.flexapp.ui.screens.bingo.figure_selection.BingoFigureSelectionScreen
import co.jarias.flexapp.ui.screens.bingo.figure_selection.BingoFigureSelectionScreenViewModel
import co.jarias.flexapp.ui.screens.bingo.game_list.BingoGameListScreen
import co.jarias.flexapp.ui.screens.bingo.game_list.BingoGameListScreenViewModel
import co.jarias.flexapp.ui.screens.bingo.game_play.BingoGamePlayScreen
import co.jarias.flexapp.ui.screens.bingo.game_play.BingoGamePlayScreenViewModel
import co.jarias.flexapp.ui.screens.bingo.game_setup.BingoGameSetupScreen
import co.jarias.flexapp.ui.screens.bingo.game_setup.BingoGameSetupScreenViewModel
import co.jarias.flexapp.ui.screens.tools.ToolSelectionScreen
import co.jarias.flexapp.ui.screens.tools.ToolSelectionScreenViewModel
import co.jarias.flexapp.ui.screens.welcome.WelcomeScreen
import co.jarias.flexapp.ui.screens.welcome.WelcomeScreenViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.WELCOME
    ) {
        composable(AppDestinations.WELCOME) {
            val viewModel: WelcomeScreenViewModel = koinViewModel()
            val state by viewModel.state.collectAsState()

            WelcomeScreen(
                onNavigate = { event -> handleNavigationEvent(navController, event) },
                onEvent = { event, nav -> viewModel.onEvent(event, nav) },
                state = state,
                modifier = Modifier
            )
        }

        composable(AppDestinations.TOOL_SELECTION) {
            val viewModel: ToolSelectionScreenViewModel = koinViewModel()
            val state by viewModel.state.collectAsState()

            ToolSelectionScreen(
                onNavigate = { event -> handleNavigationEvent(navController, event) },
                onEvent = { event -> viewModel.onEvent(event) },
                state = state
            )
        }

        composable(AppDestinations.BINGO_GAME_LIST) {
            val viewModel: BingoGameListScreenViewModel = koinViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(state.continueToCardSetup, state.continueToFigureSelection, state.continueToGamePlay) {
                when {
                    state.continueToCardSetup != null -> {
                        navController.navigate(AppDestinations.bingoCardSetupRoute(state.continueToCardSetup!!))
                        viewModel.clearNavigationState()
                    }
                    state.continueToFigureSelection != null -> {
                        navController.navigate(AppDestinations.bingoFigureSelectionRoute(state.continueToFigureSelection!!))
                        viewModel.clearNavigationState()
                    }
                    state.continueToGamePlay != null -> {
                        navController.navigate(AppDestinations.bingoGamePlayRoute(state.continueToGamePlay!!))
                        viewModel.clearNavigationState()
                    }
                }
            }

            BingoGameListScreen(
                onNavigate = { event -> handleNavigationEvent(navController, event) },
                onEvent = { event -> viewModel.onEvent(event) },
                state = state
            )
        }

        composable(AppDestinations.BINGO_GAME_SETUP) {
            val viewModel: BingoGameSetupScreenViewModel = koinViewModel()
            val state by viewModel.state.collectAsState()

            BingoGameSetupScreen(
                onNavigate = { event -> handleNavigationEvent(navController, event) },
                onEvent = { event -> viewModel.onEvent(event) },
                state = state
            )
        }

        composable(
            route = AppDestinations.BINGO_CARD_SETUP_ROUTE,
            arguments = listOf(
                navArgument(NavArguments.GAME_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong(NavArguments.GAME_ID) ?: 0L
            val viewModel: BingoCardSetupScreenViewModel = koinViewModel { parametersOf(gameId) }
            val state by viewModel.state.collectAsState()

            BingoCardSetupScreen(
                gameId = gameId,
                onNavigate = { event -> handleNavigationEvent(navController, event) },
                onEvent = { event -> viewModel.onEvent(event) },
                state = state
            )
        }

        composable(
            route = AppDestinations.BINGO_FIGURE_SELECTION_ROUTE,
            arguments = listOf(
                navArgument(NavArguments.GAME_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong(NavArguments.GAME_ID) ?: 0L
            val viewModel: BingoFigureSelectionScreenViewModel = koinViewModel { parametersOf(gameId) }
            val state by viewModel.state.collectAsState()

            BingoFigureSelectionScreen(
                gameId = gameId,
                onNavigate = { event -> handleNavigationEvent(navController, event) },
                onEvent = { event -> viewModel.onEvent(event) },
                state = state
            )
        }

        composable(
            route = AppDestinations.BINGO_GAME_PLAY_ROUTE,
            arguments = listOf(
                navArgument(NavArguments.GAME_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong(NavArguments.GAME_ID) ?: 0L
            val viewModel: BingoGamePlayScreenViewModel = koinViewModel { parametersOf(gameId) }
            val state by viewModel.state.collectAsState()

            BingoGamePlayScreen(
                onNavigate = { event -> handleNavigationEvent(navController, event) },
                onEvent = { event -> viewModel.onEvent(event) },
                state = state
            )
        }
    }
}
