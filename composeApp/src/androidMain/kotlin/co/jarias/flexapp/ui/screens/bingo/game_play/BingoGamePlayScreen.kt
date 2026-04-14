package co.jarias.flexapp.ui.screens.bingo.game_play

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import co.jarias.flexapp.domain.BingoCard
import co.jarias.flexapp.domain.BingoCell
import co.jarias.flexapp.domain.GameState
import co.jarias.flexapp.domain.WinCondition
import co.jarias.flexapp.ui.navigation.NavigationEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoGamePlayScreen(
    gameId: Long,
    onNavigate: (NavigationEvent) -> Unit,
    onEvent: (BingoGamePlayScreenEvents) -> Unit,
    state: BingoGamePlayScreenState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.gameState?.game?.name ?: "Bingo Game") },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(NavigationEvent.OnNavigateUp) }) {
                        Text("←", fontSize = 20.sp)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { onEvent(BingoGamePlayScreenEvents.OnRetryClicked) }) {
                            Text("Retry")
                        }
                    }
                }
                state.gameState != null -> {
                    BingoGameContent(
                        gameState = state.gameState,
                        onNumberClicked = { number ->
                            if (number != null) {
                                onEvent(BingoGamePlayScreenEvents.OnNumberMarked(number))
                            }
                        }
                    )
                }
            }

            if (state.showWinDialog) {
                WinDialog(
                    gameName = state.gameState?.game?.name ?: "",
                    targetFigure = state.gameState?.game?.targetFigure?.displayName ?: "",
                    onDismiss = { onEvent(BingoGamePlayScreenEvents.OnWinDialogDismissed) },
                    onBackToMenu = { onNavigate(NavigationEvent.NavigateToBingoGameList) }
                )
            }
        }
    }
}

@Composable
private fun BingoGameContent(
    gameState: GameState,
    onNumberClicked: (Int?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = gameState.game.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Target: ${gameState.game.targetFigure?.displayName ?: "Not set"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val targetCells = gameState.game.targetFigure?.requiredCells?.size ?: 0
                val markedWinningCells = gameState.game.targetFigure?.requiredCells?.count { (row, col) ->
                    val cell = gameState.card.grid[row][col]
                    cell.isFree || (cell.number != null && gameState.markedNumbers.contains(cell.number))
                } ?: 0

                if (targetCells > 0) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Progress: $markedWinningCells / $targetCells cells",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { markedWinningCells / targetCells.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Total marked: ${gameState.markedNumbers.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BingoCardGrid(
            card = gameState.card,
            markedNumbers = gameState.markedNumbers,
            targetFigure = gameState.game.targetFigure,
            onNumberClicked = onNumberClicked
        )

        Spacer(modifier = Modifier.height(16.dp))

        NumberSelector(
            markedNumbers = gameState.markedNumbers,
            onNumberClicked = onNumberClicked
        )
    }
}

@Composable
private fun BingoCardGrid(
    card: BingoCard,
    markedNumbers: Set<Int>,
    targetFigure: WinCondition?,
    onNumberClicked: (Int?) -> Unit
) {
    val columnHeaders = listOf("B", "I", "N", "G", "O")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.12f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            columnHeaders.forEach { header ->
                Text(
                    text = header,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        card.grid.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.176f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEachIndexed { colIndex, cell ->
                    val isWinningCell = targetFigure?.requiredCells?.contains(Pair(rowIndex, colIndex)) == true
                    val isMarked = cell.number != null && markedNumbers.contains(cell.number)
                    BingoGameCell(
                        cell = cell,
                        isMarked = isMarked,
                        isWinningCell = isWinningCell,
                        onClick = { onNumberClicked(cell.number) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BingoGameCell(
    cell: BingoCell,
    isMarked: Boolean,
    isWinningCell: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        cell.isFree -> MaterialTheme.colorScheme.primary
        isMarked && isWinningCell -> MaterialTheme.colorScheme.tertiary
        isMarked -> MaterialTheme.colorScheme.primaryContainer
        isWinningCell -> MaterialTheme.colorScheme.secondaryContainer
        else -> Color.White
    }

    val textColor = when {
        cell.isFree -> Color.White
        isMarked && isWinningCell -> MaterialTheme.colorScheme.onTertiary
        isMarked -> MaterialTheme.colorScheme.onPrimaryContainer
        isWinningCell -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(
                width = if (isWinningCell && isMarked) 2.dp else 1.dp,
                color = if (isWinningCell && isMarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = !cell.isFree && !isMarked, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                cell.isFree -> "FREE"
                cell.number != null -> cell.number.toString()
                else -> ""
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NumberSelector(
    markedNumbers: Set<Int>,
    onNumberClicked: (Int?) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Tap a number to mark it",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(10),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items((1..90).toList()) { number ->
                val isMarked = markedNumbers.contains(number)
                NumberButton(
                    number = number,
                    isMarked = isMarked,
                    onClick = { onNumberClicked(number) }
                )
            }
        }
    }
}

@Composable
private fun NumberButton(
    number: Int,
    isMarked: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isMarked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isMarked) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(enabled = !isMarked, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun WinDialog(
    gameName: String,
    targetFigure: String,
    onDismiss: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 BINGO! 🎉",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Congratulations!",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You completed \"$gameName\" with $targetFigure",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Continue")
                    }
                    Button(
                        onClick = onBackToMenu,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back to Menu")
                    }
                }
            }
        }
    }
}
