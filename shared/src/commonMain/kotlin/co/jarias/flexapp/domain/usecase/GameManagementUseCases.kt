package co.jarias.flexapp.domain.usecase

import co.jarias.flexapp.data.repository.GameRepository
import co.jarias.flexapp.data.repository.BingoCardRepository
import co.jarias.flexapp.data.repository.MarkedNumberRepository
import co.jarias.flexapp.domain.Game

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
    private val bingoCardRepository: BingoCardRepository,
    private val markedNumberRepository: MarkedNumberRepository
) {
    suspend operator fun invoke(gameId: Long) {
        // Delete marked numbers
        markedNumberRepository.clearMarkedNumbersForGame(gameId)

        // Delete cards
        val cards = bingoCardRepository.getCardsByGameId(gameId)
        cards.forEach { card ->
            if (card.id != null) {
                bingoCardRepository.deleteCard(card.id!!)
            }
        }

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

