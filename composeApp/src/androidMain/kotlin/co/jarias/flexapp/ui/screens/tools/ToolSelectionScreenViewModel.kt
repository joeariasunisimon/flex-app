package co.jarias.flexapp.ui.screens.tools

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ToolSelectionScreenViewModel : ViewModel() {

    private val _state = MutableStateFlow(ToolSelectionScreenState())
    val state: StateFlow<ToolSelectionScreenState> = _state.asStateFlow()

    fun onEvent(event: ToolSelectionScreenEvents) {
        when (event) {
            is ToolSelectionScreenEvents.OnBingoSelected -> {
                // Navigation handled by Screen directly via onNavigate
            }
        }
    }
}
