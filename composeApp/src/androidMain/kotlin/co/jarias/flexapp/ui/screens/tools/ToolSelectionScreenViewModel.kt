package co.jarias.flexapp.ui.screens.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.data.local.ToolType
import co.jarias.flexapp.domain.usecase.SetLastToolUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ToolSelectionScreenViewModel(
    private val setLastToolUseCase: SetLastToolUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        ToolSelectionScreenState()
    )
    val state: StateFlow<ToolSelectionScreenState> = _state.asStateFlow()

    fun onEvent(event: ToolSelectionScreenEvents) {
        viewModelScope.launch {
            when (event) {
                is ToolSelectionScreenEvents.OnBingoSelected -> {
                    setLastToolUseCase(ToolType.BINGO)
                }
            }
        }
    }
}
