package co.jarias.flexapp.di

import co.jarias.flexapp.ui.screens.bingo.card_setup.BingoCardSetupScreenViewModel
import co.jarias.flexapp.ui.screens.bingo.figure_selection.BingoFigureSelectionScreenViewModel
import co.jarias.flexapp.ui.screens.bingo.game_list.BingoGameListScreenViewModel
import co.jarias.flexapp.ui.screens.bingo.game_play.BingoGamePlayScreenViewModel
import co.jarias.flexapp.ui.screens.bingo.game_setup.BingoGameSetupScreenViewModel
import co.jarias.flexapp.ui.screens.tools.ToolSelectionScreenViewModel
import co.jarias.flexapp.ui.screens.welcome.WelcomeScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { WelcomeScreenViewModel(get()) }
    viewModel { ToolSelectionScreenViewModel(get()) }
    viewModel { BingoGameListScreenViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { BingoGameSetupScreenViewModel(get(), get()) }
    viewModel { (gameId: Long) -> BingoCardSetupScreenViewModel(get(), get(), gameId) }
    viewModel { (gameId: Long) -> BingoFigureSelectionScreenViewModel(get(), get(), gameId) }
    viewModel { (gameId: Long) -> BingoGamePlayScreenViewModel(get(), get(), get(), gameId) }
}
