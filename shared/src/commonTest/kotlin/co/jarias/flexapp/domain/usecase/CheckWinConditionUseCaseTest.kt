package co.jarias.flexapp.domain.usecase

import co.jarias.flexapp.domain.BingoCard
import co.jarias.flexapp.domain.BingoCell
import co.jarias.flexapp.domain.WinCondition
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckWinConditionUseCaseTest {

    private val testCard = BingoCard(
        id = 1,
        gameId = 1,
        grid = listOf(
            listOf(BingoCell(5, false, false), BingoCell(17, false, false), BingoCell(32, false, false), BingoCell(48, false, false), BingoCell(62, false, false)),
            listOf(BingoCell(3, false, false), BingoCell(22, false, false), BingoCell(39, false, false), BingoCell(55, false, false), BingoCell(71, false, false)),
            listOf(BingoCell(7, false, false), BingoCell(19, false, false), BingoCell(null, false, true), BingoCell(51, false, false), BingoCell(68, false, false)),
            listOf(BingoCell(11, false, false), BingoCell(25, false, false), BingoCell(41, false, false), BingoCell(58, false, false), BingoCell(73, false, false)),
            listOf(BingoCell(2, false, false), BingoCell(20, false, false), BingoCell(35, false, false), BingoCell(50, false, false), BingoCell(70, false, false))
        )
    )

    @Test
    fun checkWinCondition_returnsFalse_whenNullCondition() {
        val markedNumbers = setOf(5, 17, 32, 48, 62)
        val result = checkWinCondition(testCard, null, markedNumbers)
        assertFalse(result)
    }

    @Test
    fun checkWinCondition_returnsTrue_whenAllBColumnMarked() {
        val markedNumbers = setOf(5, 3, 7, 11, 2) // B column numbers
        val result = checkWinCondition(testCard, WinCondition.B, markedNumbers)
        assertTrue(result)
    }

    @Test
    fun checkWinCondition_returnsFalse_whenNotAllBColumnMarked() {
        val markedNumbers = setOf(5, 3, 7, 11) // B column - missing 2
        val result = checkWinCondition(testCard, WinCondition.B, markedNumbers)
        assertFalse(result)
    }

    @Test
    fun checkWinCondition_returnsTrue_whenAllIColumnMarked() {
        val markedNumbers = setOf(17, 22, 19, 25, 20) // I column numbers
        val result = checkWinCondition(testCard, WinCondition.I, markedNumbers)
        assertTrue(result)
    }

    @Test
    fun checkWinCondition_returnsTrue_whenAllNColumnMarked() {
        val markedNumbers = setOf(32, 39, 41, 35) // N column numbers (center is free)
        val result = checkWinCondition(testCard, WinCondition.N, markedNumbers)
        assertTrue(result)
    }

    @Test
    fun checkWinCondition_returnsTrue_whenAllGColumnMarked() {
        val markedNumbers = setOf(48, 55, 51, 58, 50) // G column numbers
        val result = checkWinCondition(testCard, WinCondition.G, markedNumbers)
        assertTrue(result)
    }

    @Test
    fun checkWinCondition_returnsTrue_whenAllOColumnMarked() {
        val markedNumbers = setOf(62, 71, 68, 73, 70) // O column numbers
        val result = checkWinCondition(testCard, WinCondition.O, markedNumbers)
        assertTrue(result)
    }

    @Test
    fun checkWinCondition_returnsTrue_whenFullCardMarked() {
        val markedNumbers = setOf(5, 17, 32, 48, 62, 3, 22, 39, 55, 71, 7, 19, 51, 68, 11, 25, 41, 58, 73, 2, 20, 35, 50, 70)
        val result = checkWinCondition(testCard, WinCondition.FULL_CARD, markedNumbers)
        assertTrue(result)
    }

    @Test
    fun checkWinCondition_returnsTrue_whenFreeSpaceIncluded() {
        val markedNumbers = setOf(32, 39, 41, 35) // N column with center free
        val result = checkWinCondition(testCard, WinCondition.N, markedNumbers)
        assertTrue(result)
    }

    private fun checkWinCondition(card: BingoCard, winCondition: WinCondition?, markedNumbers: Set<Int>): Boolean {
        if (winCondition == null) return false

        return winCondition.requiredCells.all { (row, col) ->
            val cell = card.grid[row][col]
            cell.isFree || (cell.number != null && markedNumbers.contains(cell.number))
        }
    }
}