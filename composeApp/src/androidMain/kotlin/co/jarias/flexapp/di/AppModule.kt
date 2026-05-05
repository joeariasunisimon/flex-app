package co.jarias.flexapp.di

import co.jarias.flexapp.providers.CameraManager
import co.jarias.flexapp.providers.NetworkManager
import co.jarias.flexapp.providers.NotificationProvider
import co.jarias.flexapp.ui.screens.bingo.card_setup.BingoCardSetupScreenViewModel
import co.jarias.flexapp.ui.screens.bingo.card_scanner.BingoCardScannerScreenViewModel
import co.jarias.flexapp.ui.screens.bingo.figure_selection.BingoFigureSelectionScreenViewModel
import co.jarias.flexapp.ui.screens.bingo.game_list.BingoGameListScreenViewModel
import co.jarias.flexapp.ui.screens.bingo.game_play.BingoGamePlayScreenViewModel
import co.jarias.flexapp.ui.screens.bingo.game_setup.BingoGameSetupScreenViewModel
import co.jarias.flexapp.ui.screens.tools.ToolSelectionScreenViewModel
import co.jarias.flexapp.ui.screens.welcome.WelcomeScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Providers
    single { CameraManager(get()) }
    single { NetworkManager(get()) }
    single { NotificationProvider(get()) }

    viewModel { WelcomeScreenViewModel(get()) }
    viewModel { ToolSelectionScreenViewModel(get()) }
    viewModel { BingoGameListScreenViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { BingoGameSetupScreenViewModel(get(), get()) }
    viewModel { (gameId: Long) ->
        BingoCardSetupScreenViewModel(
            get(), get(), get(), get(), get(), gameId
        )
    }
    viewModel { (gameId: Long) -> BingoCardScannerScreenViewModel(get(), get(), gameId) }
    viewModel { (gameId: Long) -> BingoFigureSelectionScreenViewModel(get(), get(), gameId) }
    viewModel { (gameId: Long) -> BingoGamePlayScreenViewModel(get(), get(), get(), gameId) }
}
