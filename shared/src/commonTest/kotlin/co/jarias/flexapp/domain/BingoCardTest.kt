package co.jarias.flexapp.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BingoCardTest {

    @Test
    fun generateRandomCard_returns5x5Grid() {
        val grid = BingoCard.generateRandomCard()

        assertEquals(5, grid.size)
        grid.forEach { row ->
            assertEquals(5, row.size)
        }
    }

    @Test
    fun generateRandomCard_hasFreeSpaceInCenter() {
        val grid = BingoCard.generateRandomCard()

        assertTrue(grid[2][2].isFree)
        assertNull(grid[2][2].number)
    }

    @Test
    fun generateRandomCard_freeSpaceIsNotMarked() {
        val grid = BingoCard.generateRandomCard()

        assertFalse(grid[2][2].isMarked)
    }

    @Test
    fun generateRandomCard_nonFreeSpacesHaveNumbers() {
        val grid = BingoCard.generateRandomCard()

        for (row in 0..4) {
            for (col in 0..4) {
                if (row != 2 || col != 2) {
                    assertNotNull(grid[row][col].number)
                }
            }
        }
    }

    @Test
    fun generateRandomCard_numbersInValidRanges() {
        val grid = BingoCard.generateRandomCard()

        for (row in 0..4) {
            for (col in 0..4) {
                val number = grid[row][col].number
                if (number != null) {
                    when (col) {
                        0 -> assertTrue(number in 1..15)   // B column
                        1 -> assertTrue(number in 16..30)  // I column
                        2 -> assertTrue(number in 31..45)  // N column
                        3 -> assertTrue(number in 46..60)  // G column
                        4 -> assertTrue(number in 61..75)  // O column
                    }
                }
            }
        }
    }

    @Test
    fun createCardFromGrid_createsCorrectGrid() {
        val numbers: List<List<Int?>> = listOf(
            listOf(5, 17, 32, 48, 62),
            listOf(3, 22, 39, 55, 71),
            listOf(7, 19, null, 51, 68),
            listOf(11, 25, 41, 58, 73),
            listOf(2, 20, 35, 50, 70)
        )

        val grid = BingoCard.createCardFromGrid(numbers)

        assertEquals(5, grid.size)
        assertEquals(5, grid[0].size)
        assertEquals(5, grid[0][0].number)
        assertFalse(grid[0][0].isMarked)
        assertFalse(grid[0][0].isFree)
    }

    @Test
    fun createCardFromGrid_setsFreeSpaceCorrectly() {
        val numbers: List<List<Int?>> = List(5) { List(5) { 1 } }

        val grid = BingoCard.createCardFromGrid(numbers)

        assertTrue(grid[2][2].isFree)
        assertEquals(1, grid[2][2].number)
    }

    @Test
    fun serializeGrid_producesValidJson() {
        val grid = BingoCard.generateRandomCard()
        val json = BingoCard.serializeGrid(grid)

        assertTrue(json.isNotEmpty())
        assertTrue(json.contains("["))
    }

    @Test
    fun deserializeGrid_restoresOriginalGrid() {
        val originalGrid = BingoCard.generateRandomCard()
        val json = BingoCard.serializeGrid(originalGrid)
        val restoredGrid = BingoCard.deserializeGrid(json)

        assertEquals(5, restoredGrid.size)
    }
}