package co.jarias.flexapp.ui.screens.bingo.card_setup

sealed class BingoCardSetupScreenEvents {
    data class OnCardNumberChanged(val row: Int, val col: Int, val value: String) : BingoCardSetupScreenEvents()
    data object OnNextColumn : BingoCardSetupScreenEvents()
    data object OnPreviousColumn : BingoCardSetupScreenEvents()
    data object OnSaveCard : BingoCardSetupScreenEvents()
    data object OnRandomFill : BingoCardSetupScreenEvents()
}
