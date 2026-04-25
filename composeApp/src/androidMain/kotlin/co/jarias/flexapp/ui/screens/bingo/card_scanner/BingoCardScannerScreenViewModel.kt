package co.jarias.flexapp.ui.screens.bingo.card_scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.data.local.PreferencesManager
import co.jarias.flexapp.data.repository.BingoCardRepository
import co.jarias.flexapp.domain.BingoCard
import co.jarias.flexapp.domain.BingoCell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BingoCardScannerScreenViewModel(
    private val bingoCardRepository: BingoCardRepository,
    private val preferencesManager: PreferencesManager,
    private val gameId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(BingoCardScannerScreenState(gameId = gameId))
    val state: StateFlow<BingoCardScannerScreenState> = _state.asStateFlow()

    fun onEvent(event: BingoCardScannerScreenEvents) {
        when (event) {
            is BingoCardScannerScreenEvents.OnNumbersDetected -> {
                _state.value = _state.value.copy(detectedGrid = event.grid, errorMessage = null)
            }
            is BingoCardScannerScreenEvents.OnConfirmSave -> saveCard()
            is BingoCardScannerScreenEvents.OnRetry -> {
                _state.value = _state.value.copy(detectedGrid = null, errorMessage = null)
            }
        }
    }

    private fun saveCard() {
        val gridStrings = _state.value.detectedGrid ?: return
        
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isProcessing = true)
                
                val grid = List(5) { row ->
                    List(5) { col ->
                        if (row == 2 && col == 2) {
                            BingoCell(number = null, isFree = true)
                        } else {
                            val num = gridStrings[row][col].toIntOrNull() ?: 0
                            BingoCell(number = num, isFree = false)
                        }
                    }
                }
                
                val card = BingoCard(id = null, gameId = gameId, grid = grid)
                
                withContext(Dispatchers.IO) {
                    bingoCardRepository.insertCard(card)
                }
                
                preferencesManager.clearCardSetupState(gameId)
                
                _state.value = _state.value.copy(
                    isProcessing = false,
                    cardSaved = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isProcessing = false,
                    errorMessage = "Failed to save card: ${e.message}"
                )
            }
        }
    }
}
