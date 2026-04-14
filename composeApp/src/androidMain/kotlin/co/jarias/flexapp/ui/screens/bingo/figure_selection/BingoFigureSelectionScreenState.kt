package co.jarias.flexapp.ui.screens.bingo.figure_selection

import co.jarias.flexapp.domain.BingoCell
import co.jarias.flexapp.domain.WinCondition

data class BingoFigureSelectionScreenState(
    val gameId: Long = 0,
    val cardGrid: List<List<BingoCell>> = emptyList(),
    val selectedFigure: WinCondition? = null,
    val customPattern: Set<Pair<Int, Int>> = emptySet(),
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val gameReady: Boolean = false
)
