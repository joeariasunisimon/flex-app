package co.jarias.flexapp.domain.usecase

import co.jarias.flexapp.data.repository.BingoCardRepository
import co.jarias.flexapp.data.repository.GameRepository
import co.jarias.flexapp.data.repository.MarkedNumberRepository
import co.jarias.flexapp.domain.BingoCard
import co.jarias.flexapp.domain.GameState
import co.jarias.flexapp.domain.MarkedNumber
import co.jarias.flexapp.domain.WinCondition
import kotlin.time.Clock

class GenerateBingoCardUseCase(private val bingoCardRepository: BingoCardRepository) {
    suspend operator fun invoke(gameId: Long): BingoCard {
        val grid = BingoCard.generateRandomCard()
        val card = BingoCard(gameId = gameId, grid = grid)
        bingoCardRepository.insertCard(card)
        return card
    }
}

class MarkNumberUseCase(
    private val markedNumberRepository: MarkedNumberRepository,
    private val bingoCardRepository: BingoCardRepository
) {
    suspend operator fun invoke(gameId: Long, number: Int): Boolean {
        // Check if number is already marked
        val markedNumbers = markedNumberRepository.getMarkedNumbersByGameId(gameId)
        if (markedNumbers.any { it.number == number }) {
            return false // Already marked
        }

        markedNumberRepository.insertMarkedNumber(
            MarkedNumber(gameId = gameId, number = number)
        )
        return true
    }
}

class CheckWinConditionUseCase(
    private val bingoCardRepository: BingoCardRepository,
    private val markedNumberRepository: MarkedNumberRepository
) {
    suspend operator fun invoke(gameId: Long, winCondition: WinCondition?): Boolean {
        if (winCondition == null) return false
        val cards = bingoCardRepository.getCardsByGameId(gameId)
        if (cards.isEmpty()) return false

        val card = cards.first() // Assuming one card per game for now
        val markedNumbers = markedNumberRepository.getMarkedNumbersByGameId(gameId)
        val markedSet = markedNumbers.map { it.number }.toSet()

        return winCondition.requiredCells.all { (row, col) ->
            val cell = card.grid[row][col]
            cell.isFree || (cell.number != null && markedSet.contains(cell.number))
        }
    }
}

class CompleteGameUseCase(
    private val gameRepository: GameRepository,
    private val markedNumberRepository: MarkedNumberRepository
) {
    suspend operator fun invoke(gameId: Long) {
        val completedAt = Clock.System.now().toString()
        gameRepository.updateGameCompletion(gameId, true, completedAt)
        // Optionally clear marked numbers or keep them for history
        // markedNumberRepository.clearMarkedNumbersForGame(gameId)
    }
}

class GetGameStateUseCase(
    private val gameRepository: GameRepository,
    private val bingoCardRepository: BingoCardRepository,
    private val markedNumberRepository: MarkedNumberRepository,
    private val checkWinConditionUseCase: CheckWinConditionUseCase
) {
    suspend operator fun invoke(gameId: Long): GameState? {
        val game = gameRepository.getGameById(gameId) ?: return null
        val cards = bingoCardRepository.getCardsByGameId(gameId)
        val markedNumbers = markedNumberRepository.getMarkedNumbersByGameId(gameId)

        if (cards.isEmpty()) return null

        val card = cards.first()
        val markedSet = markedNumbers.map { it.number }.toSet()
        val isWon = checkWinConditionUseCase(gameId, game.targetFigure)

        return GameState(
            game = game,
            card = card,
            markedNumbers = markedSet,
            isWon = isWon
        )
    }
}
