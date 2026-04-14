package co.jarias.flexapp.ui.screens.bingo.figure_selection

import co.jarias.flexapp.domain.WinCondition

sealed class BingoFigureSelectionScreenEvents {
    data class OnFigureSelected(val figure: WinCondition) : BingoFigureSelectionScreenEvents()
    data class OnCustomPatternToggled(val row: Int, val col: Int) : BingoFigureSelectionScreenEvents()
    data object OnContinue : BingoFigureSelectionScreenEvents()
}
