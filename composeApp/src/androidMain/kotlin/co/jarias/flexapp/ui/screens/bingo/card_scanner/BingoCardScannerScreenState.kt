package co.jarias.flexapp.ui.screens.bingo.card_scanner

data class BingoCardScannerScreenState(
    val gameId: Long = 0,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val detectedGrid: List<List<String>>? = null,
    val errorMessage: String? = null,
    val scanErrorMessage: String? = null, // For the modal error
    val cardSaved: Boolean = false,
    val isFlashOn: Boolean = false,
    val isBackCamera: Boolean = true,
    val navigateBack: Boolean = false
)
