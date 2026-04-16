package co.jarias.flexapp.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameTest {

    @Test
    fun game_defaultValues() {
        val game = Game(
            name = "Test Game",
            createdAt = "2024-01-01"
        )

        assertEquals("Test Game", game.name)
        assertEquals("2024-01-01", game.createdAt)
        assertNull(game.id)
        assertNull(game.targetFigure)
        assertFalse(game.isCompleted)
        assertNull(game.completedAt)
    }

    @Test
    fun game_withAllFields() {
        val game = Game(
            id = 1,
            name = "Test Game",
            targetFigure = WinCondition.B,
            createdAt = "2024-01-01",
            isCompleted = true,
            completedAt = "2024-01-02"
        )

        assertEquals(1, game.id)
        assertEquals("Test Game", game.name)
        assertEquals(WinCondition.B, game.targetFigure)
        assertEquals("2024-01-01", game.createdAt)
        assertTrue(game.isCompleted)
        assertEquals("2024-01-02", game.completedAt)
    }

    @Test
    fun game_copy_updatesFields() {
        val game = Game(
            id = 1,
            name = "Original",
            createdAt = "2024-01-01"
        )

        val updated = game.copy(
            name = "Updated",
            isCompleted = true,
            completedAt = "2024-01-02"
        )

        assertEquals(1, updated.id)
        assertEquals("Updated", updated.name)
        assertTrue(updated.isCompleted)
        assertEquals("2024-01-02", updated.completedAt)
    }

    @Test
    fun markedNumber_defaultValues() {
        val markedNumber = MarkedNumber(
            gameId = 1,
            number = 42
        )

        assertNull(markedNumber.id)
        assertEquals(1, markedNumber.gameId)
        assertEquals(42, markedNumber.number)
    }

    @Test
    fun markedNumber_withId() {
        val markedNumber = MarkedNumber(
            id = 1,
            gameId = 1,
            number = 42
        )

        assertEquals(1, markedNumber.id)
        assertEquals(1, markedNumber.gameId)
        assertEquals(42, markedNumber.number)
    }

    @Test
    fun gameState_defaultValues() {
        val game = Game(id = 1, name = "Test", createdAt = "2024-01-01")
        val card = BingoCard(
            id = 1,
            gameId = 1,
            grid = List(5) { List(5) { BingoCell() } }
        )

        val state = GameState(
            game = game,
            card = card,
            markedNumbers = setOf(1, 2, 3)
        )

        assertEquals(game, state.game)
        assertEquals(card, state.card)
        assertEquals(3, state.markedNumbers.size)
        assertFalse(state.isWon)
    }

    @Test
    fun gameState_withWin() {
        val game = Game(id = 1, name = "Test", createdAt = "2024-01-01")
        val card = BingoCard(
            id = 1,
            gameId = 1,
            grid = List(5) { List(5) { BingoCell() } }
        )

        val state = GameState(
            game = game,
            card = card,
            markedNumbers = setOf(1, 2, 3),
            isWon = true
        )

        assertTrue(state.isWon)
    }
}