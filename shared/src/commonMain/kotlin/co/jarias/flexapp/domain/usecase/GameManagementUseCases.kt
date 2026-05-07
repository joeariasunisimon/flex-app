package co.jarias.flexapp.domain.usecase

import co.jarias.flexapp.data.repository.GameRepository
import co.jarias.flexapp.data.repository.BingoCardRepository
import co.jarias.flexapp.data.repository.MarkedNumberRepository
import co.jarias.flexapp.data.local.PreferencesManager
import co.jarias.flexapp.domain.Game
import co.jarias.flexapp.domain.WinCondition

class GetGameByIdUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(gameId: Long): Game? {
        return gameRepository.getGameById(gameId)
    }
}

class RestartGameUseCase(
    private val gameRepository: GameRepository,
    private val markedNumberRepository: MarkedNumberRepository
) {
    suspend operator fun invoke(gameId: Long) {
        val game = gameRepository.getGameById(gameId)
        if (game != null) {
            // Reset the game completion status
            gameRepository.updateGameCompletion(gameId, false, null)
            // Clear all marked numbers
            markedNumberRepository.clearMarkedNumbersForGame(gameId)
        }
    }
}

class DropGameUseCase(
    private val gameRepository: GameRepository,
    private val markedNumberRepository: MarkedNumberRepository
) {
    suspend operator fun invoke(gameId: Long) {
        // Delete marked numbers
        markedNumberRepository.clearMarkedNumbersForGame(gameId)

        // Delete game
        gameRepository.deleteGame(gameId)
    }
}

class UpdateGameNameUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(gameId: Long, newName: String) {
        val game = gameRepository.getGameById(gameId)
        if (game != null) {
            val updatedGame = game.copy(name = newName)
            gameRepository.updateGame(updatedGame)
        }
    }
}

class UpdateGameFigureUseCase(
    private val gameRepository: GameRepository,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(gameId: Long, winCondition: WinCondition) {
        val game = gameRepository.getGameById(gameId)
        if (game != null) {
            val updatedGame = game.copy(targetFigure = winCondition)
            gameRepository.updateGame(updatedGame)
            preferencesManager.removePendingSetupGameId(gameId)
        }
    }
}

sealed class GameSetupStatus {
    object CardSetupRequired : GameSetupStatus()
    object FigureSelectionRequired : GameSetupStatus()
    object SetupComplete : GameSetupStatus()
}

class CheckGameSetupStatusUseCase(
    private val gameRepository: GameRepository,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(gameId: Long): GameSetupStatus {
        val game = gameRepository.getGameById(gameId)
        if (game?.cardId == null) {
            return GameSetupStatus.CardSetupRequired
        }

        if (game.targetFigure == null) {
            return GameSetupStatus.FigureSelectionRequired
        }

        // If complete, remove from pending
        preferencesManager.removePendingSetupGameId(gameId)
        return GameSetupStatus.SetupComplete
    }
}

