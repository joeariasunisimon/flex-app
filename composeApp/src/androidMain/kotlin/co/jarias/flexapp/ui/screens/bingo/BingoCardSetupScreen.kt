package co.jarias.flexapp.ui.screens.bingo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import co.jarias.flexapp.data.local.DatabaseDriverFactory
import co.jarias.flexapp.data.local.Database
import co.jarias.flexapp.data.repository.BingoCardRepositoryImpl
import androidx.compose.ui.platform.LocalContext
import co.jarias.flexapp.domain.BingoCard
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.Alignment
import kotlinx.coroutines.withContext

private val columnLabels = listOf("B", "I", "N", "G", "O")
private val columnRanges = listOf(1..15, 16..30, 31..45, 46..60, 61..75)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoCardSetupScreen(
    gameId: Long,
    onCardSaved: () -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var cardNumbers by remember { mutableStateOf(List(5) { List(5) { "" } }) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentStep by remember { mutableStateOf(0) } // 0-4 for each column

    val database = remember {
        Database(DatabaseDriverFactory(context))
    }
    val bingoCardRepository = remember {
        BingoCardRepositoryImpl(database)
    }

    fun validateColumn(col: Int): Boolean {
        val seen = mutableSetOf<Int>()
        for (row in 0..4) {
            if (row == 2 && col == 2) continue // Free space
            val value = cardNumbers[row][col]
            if (value.isBlank()) return false
            val num = value.toIntOrNull() ?: return false
            if (num !in columnRanges[col]) return false
            if (!seen.add(num)) return false
        }
        return true
    }

    fun validateCard(): Boolean {
        for (col in 0..4) {
            if (!validateColumn(col)) return false
        }
        return true
    }

    fun getCardNumbers(): List<List<Int?>> {
        return List(5) { row ->
            List(5) { col ->
                if (row == 2 && col == 2) null else cardNumbers[row][col].toIntOrNull()
            }
        }
    }

    LaunchedEffect(gameId) {
        val existingCard = withContext(Dispatchers.IO) {
            bingoCardRepository.getCardByGameId(gameId)
        }
        if (existingCard != null) {
            cardNumbers = existingCard.grid.map { row ->
                row.map { cell ->
                    cell.number?.toString() ?: ""
                }
            }
            // Set currentStep to the first incomplete column
            for (col in 0..4) {
                if (!validateColumn(col)) {
                    currentStep = col
                    break
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Set Up Your Bingo Card") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Text("←", style = MaterialTheme.typography.headlineSmall)
                }
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Progress indicator
            Text(
                text = "Step ${currentStep + 1} of 5: Fill the ${columnLabels[currentStep]} column",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LinearProgressIndicator(
                progress = { (currentStep + 1) / 5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "Enter 5 unique numbers between ${columnRanges[currentStep].first} and ${columnRanges[currentStep].last}. The center is a FREE space.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Current column input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = columnLabels[currentStep],
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                for (row in 0..4) {
                    if (row == 2 && currentStep == 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("FREE", textAlign = TextAlign.Center, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedTextField(
                            value = cardNumbers[row][currentStep],
                            onValueChange = {
                                if (it.length <= 2 && (it.isBlank() || it.all { ch -> ch.isDigit() })) {
                                    cardNumbers = cardNumbers.mapIndexed { idx, list ->
                                        if (idx == row) list.toMutableList().apply { this[currentStep] = it } else list
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = cardNumbers[row][currentStep].isNotBlank() && (cardNumbers[row][currentStep].toIntOrNull() !in columnRanges[currentStep]),
                            placeholder = { Text("Enter number") },
                            label = { Text("Number ${row + 1}") }
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { if (currentStep > 0) currentStep-- },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = currentStep > 0
                ) {
                    Text("Previous")
                }

                Button(
                    onClick = {
                        if (validateColumn(currentStep)) {
                            errorMessage = null
                            // Save partial card
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val grid = BingoCard.createCardFromGrid(getCardNumbers())
                                    val card = BingoCard(id = null, gameId = gameId, grid = grid)
                                    bingoCardRepository.insertCard(card)
                                } catch (e: Exception) {
                                    errorMessage = "Failed to save progress: ${e.message}"
                                }
                            }
                            if (currentStep < 4) {
                                currentStep++
                            } else {
                                onCardSaved()
                            }
                        } else {
                            errorMessage = "Please fill all fields with valid, unique numbers in the correct range."
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = validateColumn(currentStep)
                ) {
                    Text(if (currentStep == 4) "Save Card" else "Next")
                }
            }
        }
    }
}
