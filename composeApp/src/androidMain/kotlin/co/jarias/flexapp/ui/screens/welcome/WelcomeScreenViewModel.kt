package co.jarias.flexapp.ui.screens.welcome

import androidx.lifecycle.ViewModel
import co.jarias.flexapp.ui.navigation.NavigationEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WelcomeScreenViewModel : ViewModel() {

    private val _state = MutableStateFlow(WelcomeScreenState())
    val state: StateFlow<WelcomeScreenState> = _state.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    fun onEvent(event: WelcomeScreenEvents) {
        when (event) {
            is WelcomeScreenEvents.OnGetStartedClicked -> {
                _navigationEvent.value = NavigationEvent.NavigateToToolSelection
            }
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}
