package co.jarias.flexapp.ui.screens.bingo.card_setup

import co.jarias.flexapp.domain.BingoCell

data class BingoCardSetupScreenState(
    val gameId: Long = 0,
    val cardNumbers: List<List<String>> = List(5) { List(5) { "" } },
    val currentColumn: Int = 0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val cardSaved: Boolean = false
) {
    val columnLabels = listOf("B", "I", "N", "G", "O")
    val columnRanges = listOf(1..15, 16..30, 31..45, 46..60, 61..75)

    val currentColumnLabel: String get() = columnLabels.getOrElse(currentColumn) { "" }
    val currentColumnRange: IntRange get() = columnRanges.getOrElse(currentColumn) { 1..15 }
}
