package co.jarias.flexapp.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class BingoCell(
    val number: Int? = null,
    val isMarked: Boolean = false,
    val isFree: Boolean = false
)

@Serializable
data class BingoCard(
    val id: Long? = null,
    val gameId: Long,
    val grid: List<List<BingoCell>>
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun generateRandomCard(): List<List<BingoCell>> {
            val ranges = listOf(
                1..15,   // B column
                16..30,  // I column
                31..45,  // N column
                46..60,  // G column
                61..75   // O column
            )

            return List(5) { row ->
                List(5) { col ->
                    val range = ranges[col]
                    val availableNumbers = range.toMutableList()
                    // Remove numbers that might conflict, but for simplicity, just pick random
                    val number = if (row == 2 && col == 2) {
                        // FREE space in center
                        null
                    } else {
                        availableNumbers.random()
                    }
                    BingoCell(number, isMarked = false, isFree = (row == 2 && col == 2))
                }
            }
        }

        fun serializeGrid(grid: List<List<BingoCell>>): String {
            return json.encodeToString(grid)
        }

        fun deserializeGrid(jsonString: String): List<List<BingoCell>> {
            return json.decodeFromString(jsonString)
        }

        fun createCardFromGrid(numbers: List<List<Int?>>): List<List<BingoCell>> {
            return numbers.mapIndexed { row, rowList ->
                rowList.mapIndexed { col, num ->
                    BingoCell(num, isMarked = false, isFree = (row == 2 && col == 2))
                }
            }
        }
    }
}


@Serializable
sealed class WinCondition {
    abstract val displayName: String
    abstract val requiredCells: Set<Pair<Int, Int>>

    @Serializable
    data class Predefined(
        override val displayName: String,
        override val requiredCells: Set<Pair<Int, Int>>
    ) : WinCondition()

    @Serializable
    data class Custom(
        override val displayName: String = "Custom",
        override val requiredCells: Set<Pair<Int, Int>>
    ) : WinCondition()

    companion object {
        val B = Predefined("B", (0..4).map { Pair(it, 0) }.toSet())
        val I = Predefined("I", (0..4).map { Pair(it, 1) }.toSet())
        val N = Predefined("N", (0..4).map { Pair(it, 2) }.toSet())
        val G = Predefined("G", (0..4).map { Pair(it, 3) }.toSet())
        val O = Predefined("O", (0..4).map { Pair(it, 4) }.toSet())
        val FULL_CARD = Predefined("Full Card", (0..4).flatMap { row -> (0..4).map { col -> Pair(row, col) } }.toSet())

        private val json = Json { ignoreUnknownKeys = true }

        fun serialize(winCondition: WinCondition): String {
            return json.encodeToString(winCondition)
        }

        fun deserialize(jsonString: String): WinCondition {
            return json.decodeFromString(jsonString)
        }
    }
}

@Serializable
data class Game(
    val id: Long? = null,
    val name: String,
    val targetFigure: WinCondition? = null,
    val createdAt: String,
    val isCompleted: Boolean = false,
    val completedAt: String? = null
)

data class MarkedNumber(
    val id: Long? = null,
    val gameId: Long,
    val number: Int
)

// WinCondition enum replaced by sealed class above

data class GameState(
    val game: Game,
    val card: BingoCard,
    val markedNumbers: Set<Int>,
    val isWon: Boolean = false
)
