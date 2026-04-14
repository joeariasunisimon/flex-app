package co.jarias.flexapp.ui.screens.tools

import androidx.lifecycle.ViewModel
import co.jarias.flexapp.ui.navigation.NavigationEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ToolSelectionScreenViewModel : ViewModel() {

    private val _state = MutableStateFlow(ToolSelectionScreenState())
    val state: StateFlow<ToolSelectionScreenState> = _state.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    fun onEvent(event: ToolSelectionScreenEvents) {
        when (event) {
            is ToolSelectionScreenEvents.OnBingoSelected -> {
                _navigationEvent.value = NavigationEvent.NavigateToBingoGameList
            }
            is ToolSelectionScreenEvents.OnBackPressed -> {
                _navigationEvent.value = NavigationEvent.OnNavigateUp
            }
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}
