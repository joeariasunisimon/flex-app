package co.jarias.flexapp.domain.usecase

import co.jarias.flexapp.data.repository.BingoCardRepository
import co.jarias.flexapp.data.repository.GameRepository
import co.jarias.flexapp.data.repository.MarkedNumberRepository
import co.jarias.flexapp.domain.BingoCard
import co.jarias.flexapp.domain.GameState
import co.jarias.flexapp.domain.MarkedNumber
import co.jarias.flexapp.domain.WinCondition
import kotlin.random.Random
import kotlin.time.Clock

class GenerateRandomNumbersUseCase {
    private val columnRanges = listOf(1..15, 16..30, 31..45, 46..60, 61..75)

    operator fun invoke(columnIndex: Int): Set<Int> {
        val range = columnRanges.getOrElse(columnIndex) { 1..15 }
        return generateRandomNumbers(range, columnIndex == 2)
    }

    private fun generateRandomNumbers(range: IntRange, isNColumn: Boolean): Set<Int> {
        val count = if (isNColumn) 4 else 5
        return Random.nextInt(0, Int.MAX_VALUE)
            .let { seed ->
                val random = Random(seed)
                buildSet {
                    while (size < count) {
                        add(random.nextInt(range.first, range.last + 1))
                    }
                }
            }
    }
}

class GenerateBingoCardUseCase(
    private val bingoCardRepository: BingoCardRepository,
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(gameId: Long): BingoCard {
        val grid = BingoCard.generateRandomCard()
        val createdAt = Clock.System.now().toString()
        val card = BingoCard(grid = grid, createdAt = createdAt)
        val cardId = bingoCardRepository.insertCard(card)
        gameRepository.updateGameCard(gameId, cardId)
        return card.copy(id = cardId)
    }
}

class SaveBingoCardUseCase(
    private val bingoCardRepository: BingoCardRepository,
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(gameId: Long, card: BingoCard) {
        val cardId = bingoCardRepository.insertCard(card)
        gameRepository.updateGameCard(gameId, cardId)
    }
}

class GetBingoCardUseCase(
    private val bingoCardRepository: BingoCardRepository,
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(gameId: Long): BingoCard? {
        val game = gameRepository.getGameById(gameId) ?: return null
        return game.cardId?.let { bingoCardRepository.getCardById(it) }
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
    private val getBingoCardUseCase: GetBingoCardUseCase,
    private val markedNumberRepository: MarkedNumberRepository
) {
    suspend operator fun invoke(gameId: Long, winCondition: WinCondition?): Boolean {
        if (winCondition == null) return false
        val card = getBingoCardUseCase(gameId) ?: return false

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
    private val getBingoCardUseCase: GetBingoCardUseCase,
    private val bingoCardRepository: BingoCardRepository,
    private val markedNumberRepository: MarkedNumberRepository,
    private val checkWinConditionUseCase: CheckWinConditionUseCase
) {
    suspend operator fun invoke(gameId: Long): GameState? {
        val game = gameRepository.getGameById(gameId) ?: return null
        val card = getBingoCardUseCase(gameId) ?: return null
        val markedNumbers = markedNumberRepository.getMarkedNumbersByGameId(gameId)

        val markedSet = markedNumbers.map { it.number }.toSet()
        val isWon = checkWinConditionUseCase(gameId, game.targetFigure)
        
        val usageCount = card.id?.let { bingoCardRepository.getCardUsageCount(it) } ?: 1

        return GameState(
            game = game,
            card = card,
            markedNumbers = markedSet,
            isWon = isWon,
            usageCount = usageCount
        )
    }
}
