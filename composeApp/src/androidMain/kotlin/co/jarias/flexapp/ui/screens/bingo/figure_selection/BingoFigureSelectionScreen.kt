package co.jarias.flexapp.ui.screens.bingo.figure_selection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jarias.flexapp.R
import co.jarias.flexapp.domain.WinCondition
import co.jarias.flexapp.ui.navigation.NavigationEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoFigureSelectionScreen(
    gameId: Long,
    onNavigate: (NavigationEvent) -> Unit,
    onEvent: (BingoFigureSelectionScreenEvents) -> Unit,
    state: BingoFigureSelectionScreenState
) {
    val columnLabels = listOf("B", "I", "N", "G", "O")

    LaunchedEffect(state.gameReady) {
        if (state.gameReady) {
            onNavigate(NavigationEvent.NavigateToBingoGamePlay(state.gameId))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Select Win Figure") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tap a column (B, I, N, G, O) to select it as your win figure, or choose Full Card to mark all cells.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.15f),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            columnLabels.forEach { label ->
                                Text(
                                    label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        for (row in 0..4) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.17f),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (col in 0..4) {
                                    val cell = state.cardGrid.getOrNull(row)?.getOrNull(col)
                                    val isFree = cell?.isFree == true
                                    val isCustomSelected = state.customPattern.contains(Pair(row, col))
                                    val isHighlighted = state.selectedFigure?.requiredCells?.contains(Pair(row, col)) == true || isCustomSelected

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .background(
                                                when {
                                                    isFree -> MaterialTheme.colorScheme.primary
                                                    isHighlighted -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> Color.White
                                                },
                                                shape = MaterialTheme.shapes.small
                                            )
                                            .border(
                                                if (isHighlighted) 3.dp else 1.dp,
                                                if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                shape = MaterialTheme.shapes.small
                                            )
                                            .clickable(enabled = !isFree) {
                                                onEvent(BingoFigureSelectionScreenEvents.OnCustomPatternToggled(row, col))
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cell?.number?.toString() ?: "FREE",
                                            textAlign = TextAlign.Center,
                                            color = when {
                                                isFree -> Color.White
                                                isHighlighted -> MaterialTheme.colorScheme.onPrimaryContainer
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.selectedFigure != null || state.customPattern.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        state.selectedFigure?.let { figure ->
                            Text(
                                text = "Selected: ${figure.displayName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You need to mark ${figure.requiredCells.size} cells to win!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (state.customPattern.isNotEmpty()) {
                            Text(
                                text = "Custom Pattern Selected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You need to mark ${state.customPattern.size} cells to win!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    WinCondition.B,
                    WinCondition.I,
                    WinCondition.N,
                    WinCondition.G,
                    WinCondition.O
                ).forEach { colCond ->
                    OutlinedButton(
                        onClick = { onEvent(BingoFigureSelectionScreenEvents.OnFigureSelected(colCond)) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (state.selectedFigure == colCond) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Text(colCond.displayName)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onEvent(BingoFigureSelectionScreenEvents.OnFigureSelected(WinCondition.FULL_CARD)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (state.selectedFigure == WinCondition.FULL_CARD) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) {
                Text("Or select Full Card (all 25 cells)")
            }

            Spacer(modifier = Modifier.height(24.dp))

            state.errorMessage?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { onEvent(BingoFigureSelectionScreenEvents.OnContinue) },
                modifier = Modifier.fillMaxWidth(),
                enabled = (state.selectedFigure != null || state.customPattern.isNotEmpty()) && !state.isUpdating
            ) {
                if (state.isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
