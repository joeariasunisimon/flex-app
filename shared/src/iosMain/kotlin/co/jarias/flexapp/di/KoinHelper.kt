package co.jarias.flexapp.di

import co.jarias.flexapp.domain.usecase.*
import co.jarias.flexapp.data.repository.*
import co.jarias.flexapp.data.local.PreferencesManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class KoinHelper : KoinComponent {
    val getAllGamesUseCase: GetAllGamesUseCase by inject()
    val restartGameUseCase: RestartGameUseCase by inject()
    val dropGameUseCase: DropGameUseCase by inject()
    val bingoCardRepository: BingoCardRepository by inject()
    val gameRepository: GameRepository by inject()
    val preferencesManager: PreferencesManager by inject()
    val createGameUseCase: CreateGameUseCase by inject()
    val getGameStateUseCase: GetGameStateUseCase by inject()
    val markNumberUseCase: MarkNumberUseCase by inject()
    val checkWinConditionUseCase: CheckWinConditionUseCase by inject()
    val completeGameUseCase: CompleteGameUseCase by inject()
    val generateBingoCardUseCase: GenerateBingoCardUseCase by inject()
    val updateGameNameUseCase: UpdateGameNameUseCase by inject()

    fun getAllGamesWatcher() = co.jarias.flexapp.util.FlowWatcher(getAllGamesUseCase())
}

fun doInitKoin() {
    initKoin()
}
