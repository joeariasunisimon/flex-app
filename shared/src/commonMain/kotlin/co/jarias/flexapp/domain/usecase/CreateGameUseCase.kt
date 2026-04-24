package co.jarias.flexapp.domain.usecase

import co.jarias.flexapp.data.repository.GameRepository
import co.jarias.flexapp.domain.Game
import co.jarias.flexapp.domain.WinCondition
import kotlin.time.Clock

class CreateGameUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(name: String, targetFigure: WinCondition? = null): Game {
        val createdAt = Clock.System.now().toString()
        val game = Game(name = name, targetFigure = targetFigure, createdAt = createdAt)
        val gameId = gameRepository.insertGame(game)
        return game.copy(id = gameId)
    }
}

class GetAllGamesUseCase(private val gameRepository: GameRepository) {
    operator fun invoke(): kotlinx.coroutines.flow.Flow<List<Game>> {
        return gameRepository.getAllGames()
    }
}
