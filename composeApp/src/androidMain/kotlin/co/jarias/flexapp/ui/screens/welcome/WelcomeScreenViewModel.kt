package co.jarias.flexapp.ui.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.data.local.PreferencesManager
import co.jarias.flexapp.data.local.ToolType
import co.jarias.flexapp.ui.navigation.NavigationEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WelcomeScreenViewModel(
    private val preferencesManager: PreferencesManager? = null
) : ViewModel() {

    private val _state = MutableStateFlow(WelcomeScreenState(isLoading = true))
    val state: StateFlow<WelcomeScreenState> = _state.asStateFlow()

    init {
        loadLastTool()
    }

    private fun loadLastTool() {
        viewModelScope.launch {
            val lastTool = preferencesManager?.getLastTool()
            _state.value = _state.value.copy(
                isLoading = false,
                lastTool = lastTool
            )
        }
    }

    fun onEvent(event: WelcomeScreenEvents, onNavigate: (NavigationEvent) -> Unit) {
        when (event) {
            is WelcomeScreenEvents.OnGetStartedClicked -> {
                val lastTool = _state.value.lastTool
                if (lastTool != null) {
                    onNavigate(NavigationEvent.NavigateToTool(lastTool))
                } else {
                    onNavigate(NavigationEvent.NavigateToToolSelection)
                }
            }
        }
    }
}