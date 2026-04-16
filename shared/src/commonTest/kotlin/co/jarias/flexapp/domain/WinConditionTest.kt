package co.jarias.flexapp.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WinConditionTest {

    @Test
    fun winCondition_B_hasCorrectDisplayName() {
        assertEquals("B", WinCondition.B.displayName)
    }

    @Test
    fun winCondition_B_hasCorrectRequiredCells() {
        val expected = (0..4).map { Pair(it, 0) }.toSet()
        assertEquals(expected, WinCondition.B.requiredCells)
    }

    @Test
    fun winCondition_I_hasCorrectRequiredCells() {
        val expected = (0..4).map { Pair(it, 1) }.toSet()
        assertEquals(expected, WinCondition.I.requiredCells)
    }

    @Test
    fun winCondition_N_hasCorrectRequiredCells() {
        val expected = (0..4).map { Pair(it, 2) }.toSet()
        assertEquals(expected, WinCondition.N.requiredCells)
    }

    @Test
    fun winCondition_G_hasCorrectRequiredCells() {
        val expected = (0..4).map { Pair(it, 3) }.toSet()
        assertEquals(expected, WinCondition.G.requiredCells)
    }

    @Test
    fun winCondition_O_hasCorrectRequiredCells() {
        val expected = (0..4).map { Pair(it, 4) }.toSet()
        assertEquals(expected, WinCondition.O.requiredCells)
    }

    @Test
    fun winCondition_FULL_CARD_has25Cells() {
        assertEquals(25, WinCondition.FULL_CARD.requiredCells.size)
    }

    @Test
    fun winCondition_FULL_CARD_hasAllPositions() {
        val expected = (0..4).flatMap { row -> (0..4).map { col -> Pair(row, col) } }.toSet()
        assertEquals(expected, WinCondition.FULL_CARD.requiredCells)
    }

    @Test
    fun serializeWinCondition_producesValidJson() {
        val json = WinCondition.serialize(WinCondition.B)

        assertTrue(json.isNotEmpty())
    }

    @Test
    fun deserializeWinCondition_restoresWinCondition() {
        val json = WinCondition.serialize(WinCondition.N)
        val restored = WinCondition.deserialize(json)

        assertEquals("N", restored.displayName)
    }

    @Test
    fun predefinedWinCondition_serializationRoundTrip() {
        val json = WinCondition.serialize(WinCondition.G)
        val restored = WinCondition.deserialize(json)

        assertEquals(WinCondition.G.displayName, restored.displayName)
    }

    @Test
    fun customWinCondition_serializationRoundTrip() {
        val customCondition = WinCondition.Custom(
            displayName = "Diagonal",
            requiredCells = setOf(Pair(0, 0), Pair(1, 1), Pair(2, 2), Pair(3, 3), Pair(4, 4))
        )

        val json = WinCondition.serialize(customCondition)
        val restored = WinCondition.deserialize(json) as WinCondition.Custom

        assertEquals("Diagonal", restored.displayName)
        assertEquals(5, restored.requiredCells.size)
    }
}