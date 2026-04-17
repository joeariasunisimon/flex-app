package co.jarias.flexapp.ui.screens.welcome

import co.jarias.flexapp.data.local.ToolType

data class WelcomeScreenState(
    val isLoading: Boolean = false,
    val lastTool: ToolType? = null
)
