package co.jarias.flexapp.ui.screens.bingo.card_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.jarias.flexapp.R
import co.jarias.flexapp.ui.navigation.NavigationEvent

private val columnLabels = listOf("B", "I", "N", "G", "O")
private val columnRanges = listOf(1..15, 16..30, 31..45, 46..60, 61..75)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoCardSetupScreen(
    gameId: Long,
    onNavigate: (NavigationEvent) -> Unit,
    onEvent: (BingoCardSetupScreenEvents) -> Unit,
    state: BingoCardSetupScreenState
) {
    LaunchedEffect(state.cardSaved) {
        if (state.cardSaved) {
            onNavigate(NavigationEvent.NavigateToBingoFigureSelection(state.gameId))
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
                IconButton(onClick = { onNavigate(NavigationEvent.OnNavigateUp) }) {
                    Icon(painter = painterResource(id = R.drawable.outline_arrow_back), contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Step ${state.currentColumn + 1} of 5: Fill the ${columnLabels.getOrElse(state.currentColumn) { "" }} column",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LinearProgressIndicator(
                progress = { (state.currentColumn + 1) / 5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "Enter 5 unique numbers between ${columnRanges.getOrElse(state.currentColumn) { 1..15 }.first} and ${columnRanges.getOrElse(state.currentColumn) { 1..15 }.last}. The center is a FREE space.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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
                    text = columnLabels.getOrElse(state.currentColumn) { "" },
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                for (row in 0..4) {
                    if (row == 2 && state.currentColumn == 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "FREE",
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = state.cardNumbers.getOrNull(row)?.getOrNull(state.currentColumn) ?: "",
                            onValueChange = { value ->
                                onEvent(BingoCardSetupScreenEvents.OnCardNumberChanged(row, state.currentColumn, value))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            singleLine = true,
                            textStyle = TextStyle(textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("Enter number") },
                            label = { Text("Number ${row + 1}") }
                        )
                    }
                }
            }

            if (state.errorMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onEvent(BingoCardSetupScreenEvents.OnPreviousColumn) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = state.currentColumn > 0
                ) {
                    Text("Previous")
                }

                Button(
                    onClick = { onEvent(BingoCardSetupScreenEvents.OnNextColumn) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (state.currentColumn == 4) "Save Card" else "Next")
                    }
                }
            }
        }
    }
}
