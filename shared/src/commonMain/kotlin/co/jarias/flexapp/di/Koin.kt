package co.jarias.flexapp.di

import co.jarias.flexapp.data.local.Database
import co.jarias.flexapp.data.repository.*
import co.jarias.flexapp.domain.usecase.*
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect val platformModule: Module

val commonModule = module {
    single { Database(get()) }
    single<GameRepository> { GameRepositoryImpl(get()) }
    single<BingoCardRepository> { BingoCardRepositoryImpl(get()) }
    single<MarkedNumberRepository> { MarkedNumberRepositoryImpl(get()) }
    single { co.jarias.flexapp.data.local.PreferencesManager(get()) }

    factory { GenerateRandomNumbersUseCase() }
    factory { GenerateBingoCardUseCase(get()) }
    factory { MarkNumberUseCase(get(), get()) }
    factory { CheckWinConditionUseCase(get(), get()) }
    factory { CompleteGameUseCase(get(), get()) }
    factory { GetGameStateUseCase(get(), get(), get(), get()) }
    factory { CreateGameUseCase(get()) }
    factory { GetAllGamesUseCase(get()) }
    factory { GetGameByIdUseCase(get()) }
    factory { RestartGameUseCase(get(), get()) }
    factory { DropGameUseCase(get(), get(), get()) }
    factory { UpdateGameNameUseCase(get()) }
}

fun initKoin(appDeclaration: KoinAppDeclaration) =
    startKoin {
        appDeclaration()
        modules(platformModule, commonModule)
    }

// For iOS or simple Android init
fun initKoin() = initKoin {}
