package co.jarias.flexapp.ui.screens.tools

sealed class ToolSelectionScreenEvents {
    data object OnBingoSelected : ToolSelectionScreenEvents()
    data object OnBackPressed : ToolSelectionScreenEvents()
}
