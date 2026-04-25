package co.jarias.flexapp.ui.screens.bingo.card_scanner

sealed class BingoCardScannerScreenEvents {
    data class OnNumbersDetected(val grid: List<List<String>>) : BingoCardScannerScreenEvents()
    data object OnConfirmSave : BingoCardScannerScreenEvents()
    data object OnRetry : BingoCardScannerScreenEvents()
}
