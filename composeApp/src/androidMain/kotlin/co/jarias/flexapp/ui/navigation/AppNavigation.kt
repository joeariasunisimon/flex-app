package co.jarias.flexapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import co.jarias.flexapp.data.local.Database
import co.jarias.flexapp.data.local.DatabaseDriverFactory
import co.jarias.flexapp.data.repository.BingoCardRepositoryImpl
import co.jarias.flexapp.data.repository.GameRepositoryImpl
import co.jarias.flexapp.data.repository.MarkedNumberRepositoryImpl
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

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current

    val gameRepository = remember {
        GameRepositoryImpl(Database(DatabaseDriverFactory(context)))
    }
    val bingoCardRepository = remember {
        BingoCardRepositoryImpl(Database(DatabaseDriverFactory(context)))
    }
    val markedNumberRepository = remember {
        MarkedNumberRepositoryImpl(Database(DatabaseDriverFactory(context)))
    }

    NavHost(
        navController = navController,
        startDestination = AppDestinations.WELCOME
    ) {
        composable(AppDestinations.WELCOME) {
            val viewModel = remember { WelcomeScreenViewModel() }
            val state by viewModel.state.collectAsState()
            val navigationEvent by viewModel.navigationEvent.collectAsState()

            LaunchedEffect(navigationEvent) {
                navigationEvent?.let {
                    handleNavigationEvent(navController, it)
                    viewModel.onNavigationHandled()
                }
            }

            WelcomeScreen(
                onNavigate = { event -> handleNavigationEvent(navController, event) },
                onEvent = { event -> viewModel.onEvent(event) },
                state = state
            )
        }

        composable(AppDestinations.TOOL_SELECTION) {
            val viewModel = remember { ToolSelectionScreenViewModel() }
            val state by viewModel.state.collectAsState()
            val navigationEvent by viewModel.navigationEvent.collectAsState()

            LaunchedEffect(navigationEvent) {
                navigationEvent?.let {
                    handleNavigationEvent(navController, it)
                    viewModel.onNavigationHandled()
                }
            }

            ToolSelectionScreen(
                onNavigate = { event -> handleNavigationEvent(navController, event) },
                onEvent = { event -> viewModel.onEvent(event) },
                state = state
            )
        }

        composable(AppDestinations.BINGO_GAME_LIST) {
            val viewModel = remember { BingoGameListScreenViewModel(gameRepository, bingoCardRepository, markedNumberRepository) }
            val state by viewModel.state.collectAsState()
            val navigationEvent by viewModel.navigationEvent.collectAsState()

            LaunchedEffect(navigationEvent) {
                navigationEvent?.let {
                    handleNavigationEvent(navController, it)
                    viewModel.onNavigationHandled()
                }
            }

            BingoGameListScreen(
                onNavigate = { event -> handleNavigationEvent(navController, event) },
                onEvent = { event -> viewModel.onEvent(event) },
                state = state
            )
        }

        composable(AppDestinations.BINGO_GAME_SETUP) {
            val viewModel = remember { BingoGameSetupScreenViewModel(gameRepository) }
            val state by viewModel.state.collectAsState()
            val navigationEvent by viewModel.navigationEvent.collectAsState()

            LaunchedEffect(navigationEvent) {
                navigationEvent?.let {
                    handleNavigationEvent(navController, it)
                    viewModel.onNavigationHandled()
                }
            }

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
            val viewModel = remember { BingoCardSetupScreenViewModel(bingoCardRepository, gameId) }
            val state by viewModel.state.collectAsState()
            val navigationEvent by viewModel.navigationEvent.collectAsState()

            LaunchedEffect(navigationEvent) {
                navigationEvent?.let {
                    handleNavigationEvent(navController, it)
                    viewModel.onNavigationHandled()
                }
            }

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
            val viewModel = remember {
                BingoFigureSelectionScreenViewModel(bingoCardRepository, gameRepository, gameId)
            }
            val state by viewModel.state.collectAsState()
            val navigationEvent by viewModel.navigationEvent.collectAsState()

            LaunchedEffect(navigationEvent) {
                navigationEvent?.let {
                    handleNavigationEvent(navController, it)
                    viewModel.onNavigationHandled()
                }
            }

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
            val viewModel = remember {
                BingoGamePlayScreenViewModel(gameRepository, bingoCardRepository, markedNumberRepository, gameId)
            }
            val state by viewModel.state.collectAsState()
            val navigationEvent by viewModel.navigationEvent.collectAsState()

            LaunchedEffect(navigationEvent) {
                navigationEvent?.let {
                    handleNavigationEvent(navController, it)
                    viewModel.onNavigationHandled()
                }
            }

            BingoGamePlayScreen(
                gameId = gameId,
                onNavigate = { event -> handleNavigationEvent(navController, event) },
                onEvent = { event -> viewModel.onEvent(event) },
                state = state
            )
        }
    }
}
