package co.jarias.flexapp.ui.screens.bingo.card_setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jarias.flexapp.data.local.CardSetupState
import co.jarias.flexapp.data.local.PreferencesManager
import co.jarias.flexapp.data.repository.BingoCardRepository
import co.jarias.flexapp.domain.BingoCard
import co.jarias.flexapp.domain.BingoCell
import co.jarias.flexapp.domain.usecase.GenerateRandomNumbersUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BingoCardSetupScreenViewModel(
    private val bingoCardRepository: BingoCardRepository,
    private val preferencesManager: PreferencesManager? = null,
    private val gameId: Long = 0
) : ViewModel() {

    private val _state = MutableStateFlow(BingoCardSetupScreenState())
    val state: StateFlow<BingoCardSetupScreenState> = _state.asStateFlow()

    private val generateRandomNumbersUseCase = GenerateRandomNumbersUseCase()

    init {
        if (gameId != 0L) {
            loadGame(gameId)
        }
    }

    private fun validateColumn(col: Int): Boolean {
        val cardNumbers = _state.value.cardNumbers
        val seen = mutableSetOf<Int>()
        for (row in 0..4) {
            if (row == 2 && col == 2) continue
            val value = cardNumbers[row][col]
            if (value.isBlank()) return false
            val num = value.toIntOrNull() ?: return false
            val ranges = listOf(1..15, 16..30, 31..45, 46..60, 61..75)
            if (num !in ranges.getOrElse(col) { 1..15 }) return false
            if (!seen.add(num)) return false
        }
        return true
    }

    private fun loadCardSetupStateFromPreferences() {
        viewModelScope.launch {
            val savedState = preferencesManager?.getCardSetupState(gameId)
            savedState?.let {
                _state.value = _state.value.copy(
                    currentColumn = it.currentColumn,
                    cardNumbers = it.cardNumbers
                )
            }
        }
    }

    private fun saveCardSetupStateToPreferences() {
        viewModelScope.launch {
            preferencesManager?.setCardSetupState(
                gameId,
                CardSetupState(
                    currentColumn = _state.value.currentColumn,
                    cardNumbers = _state.value.cardNumbers
                )
            )
        }
    }

    private fun clearCardSetupStateInPreferences() {
        viewModelScope.launch {
            preferencesManager?.clearCardSetupState(gameId)
        }
    }

    fun loadGame(gameId: Long) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, gameId = gameId)

                val existingCard = withContext(Dispatchers.IO) {
                    bingoCardRepository.getCardByGameId(gameId)
                }

                if (existingCard != null) {
                    val cardNumbers = existingCard.grid.map { row ->
                        row.map { cell ->
                            cell.number?.toString() ?: ""
                        }
                    }
                    _state.value = _state.value.copy(
                        cardNumbers = cardNumbers,
                        isLoading = false
                    )
                    findFirstIncompleteColumn()
                } else {
                    loadCardSetupStateFromPreferences()
                    _state.value = _state.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load card: ${e.message}"
                )
            }
        }
    }

    private fun findFirstIncompleteColumn() {
        for (col in 0..4) {
            if (!validateColumn(col)) {
                _state.value = _state.value.copy(currentColumn = col)
                return
            }
        }
    }

    fun onEvent(event: BingoCardSetupScreenEvents) {
        when (event) {
            is BingoCardSetupScreenEvents.OnCardNumberChanged -> updateCardNumber(event.row, event.col, event.value)
            is BingoCardSetupScreenEvents.OnNextColumn -> nextColumn()
            is BingoCardSetupScreenEvents.OnPreviousColumn -> previousColumn()
            is BingoCardSetupScreenEvents.OnSaveCard -> saveCard()
            is BingoCardSetupScreenEvents.OnRandomFill -> randomFillCurrentColumn()
        }
    }

    private fun randomFillCurrentColumn() {
        val currentColumn = _state.value.currentColumn
        val randomNumbers = generateRandomNumbersUseCase(currentColumn)
        val numberList = randomNumbers.toList()

        val newCardNumbers = _state.value.cardNumbers.mapIndexed { rowIdx, row ->
            if (rowIdx == 2 && currentColumn == 2) {
                row
            } else {
                row.mapIndexed { colIdx, value ->
                    if (colIdx == currentColumn && value.isBlank()) {
                        val numIdx = if (currentColumn == 2) {
                            if (rowIdx < 2) rowIdx else rowIdx - 1
                        } else rowIdx
                        numberList.getOrNull(numIdx)?.toString() ?: value
                    } else value
                }
            }
        }

        _state.value = _state.value.copy(
            cardNumbers = newCardNumbers,
            errorMessage = null
        )
        saveCardSetupStateToPreferences()
    }

    fun updateCardNumber(row: Int, col: Int, value: String) {
        val newCardNumbers = _state.value.cardNumbers.mapIndexed { idx, list ->
            if (idx == row) {
                list.mapIndexed { c, v -> if (c == col) value else v }
            } else list
        }
        _state.value = _state.value.copy(
            cardNumbers = newCardNumbers,
            errorMessage = null
        )
        saveCardSetupStateToPreferences()
    }

    private fun nextColumn() {
        if (!validateColumn(_state.value.currentColumn)) {
            _state.value = _state.value.copy(
                errorMessage = "Please fill all fields with valid, unique numbers in the correct range."
            )
            return
        }

        saveCardSetupStateToPreferences()

        if (_state.value.currentColumn < 4) {
            _state.value = _state.value.copy(
                currentColumn = _state.value.currentColumn + 1,
                errorMessage = null
            )
        } else {
            saveCard()
        }
    }

    private fun previousColumn() {
        saveCardSetupStateToPreferences()

        if (_state.value.currentColumn > 0) {
            _state.value = _state.value.copy(
                currentColumn = _state.value.currentColumn - 1,
                errorMessage = null
            )
        }
    }

    private fun saveCard() {
        val currentState = _state.value

        if (!validateColumn(currentState.currentColumn)) {
            _state.value = currentState.copy(
                errorMessage = "Please fill all fields with valid, unique numbers in the correct range."
            )
            return
        }

        for (col in 0..4) {
            if (!validateColumn(col)) {
                _state.value = currentState.copy(
                    currentColumn = col,
                    errorMessage = "Please fill all fields with valid, unique numbers."
                )
                return
            }
        }

        viewModelScope.launch {
            try {
                _state.value = currentState.copy(isSaving = true)

                val grid = getCardNumbers()
                val card = BingoCard(id = null, gameId = currentState.gameId, grid = grid)

                withContext(Dispatchers.IO) {
                    bingoCardRepository.insertCard(card)
                }

                clearCardSetupStateInPreferences()

                _state.value = currentState.copy(
                    isSaving = false,
                    cardSaved = true
                )
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isSaving = false,
                    errorMessage = "Failed to save card: ${e.message}"
                )
            }
        }
    }

    private fun getCardNumbers(): List<List<BingoCell>> {
        return List(5) { row ->
            List(5) { col ->
                if (row == 2 && col == 2) {
                    BingoCell(number = null, isFree = true)
                } else {
                    val num = _state.value.cardNumbers[row][col].toIntOrNull() ?: 0
                    BingoCell(number = num, isFree = false)
                }
            }
        }
    }
}