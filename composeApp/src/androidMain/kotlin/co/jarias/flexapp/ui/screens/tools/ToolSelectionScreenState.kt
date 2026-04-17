package co.jarias.flexapp.ui.screens.tools

import co.jarias.flexapp.data.local.PreferencesManager

data class ToolSelectionScreenState(
    val isLoading: Boolean = false,
    val preferencesManager: PreferencesManager? = null
)
