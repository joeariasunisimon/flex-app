package co.jarias.flexapp.ui.screens.bingo.game_play

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import co.jarias.flexapp.R
import co.jarias.flexapp.domain.BingoCard
import co.jarias.flexapp.domain.BingoCell
import co.jarias.flexapp.domain.BingoCellPos
import co.jarias.flexapp.domain.Game
import co.jarias.flexapp.domain.GameState
import co.jarias.flexapp.domain.WinCondition
import co.jarias.flexapp.ui.navigation.NavigationEvent
import co.jarias.flexapp.ui.theme.FlexAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoGamePlayScreen(
    onNavigate: (NavigationEvent) -> Unit,
    onEvent: (BingoGamePlayScreenEvents) -> Unit,
    state: BingoGamePlayScreenState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.gameState?.game?.name ?: "Bingo Game",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(NavigationEvent.OnNavigateUp) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_arrow_back),
                            contentDescription = "Back"
                        )
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
                        Button(
                            onClick = { onEvent(BingoGamePlayScreenEvents.OnRetryClicked) },
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                text = "Retry",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
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
            shape = MaterialTheme.shapes.medium
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
                val markedWinningCells =
                    gameState.game.targetFigure?.requiredCells?.count { (row, col) ->
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
            .border(
                width = 3.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium,
            )
            .background(color = Color.White, shape = MaterialTheme.shapes.medium)
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
                    val isWinningCell = targetFigure?.requiredCells?.contains(
                        BingoCellPos(
                            rowIndex,
                            colIndex
                        )
                    ) == true
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
            .background(color = backgroundColor, shape = MaterialTheme.shapes.small)
            .border(
                width = if (isWinningCell && isMarked) 2.dp else 1.dp,
                color = if (isWinningCell && isMarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small
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
            .background(color = backgroundColor, shape = MaterialTheme.shapes.large)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.large
            )
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
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 BINGO! 🎉",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Congratulations!",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You completed \"$gameName\" with $targetFigure",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text(text = "Continue", style = MaterialTheme.typography.labelMedium)
                    }
                    Button(
                        onClick = onBackToMenu,
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text(text = "Go to Menu", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Loading")
@Composable
fun BingoGamePlayScreenPreviewLoading() {
    FlexAppTheme {
        BingoGamePlayScreen(
            onNavigate = {},
            onEvent = {},
            state = BingoGamePlayScreenState(isLoading = true)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Error")
@Composable
fun BingoGamePlayScreenPreviewError() {
    FlexAppTheme {
        BingoGamePlayScreen(
            onNavigate = {},
            onEvent = {},
            state = BingoGamePlayScreenState(
                isLoading = false,
                errorMessage = "Failed to load game. Please try again."
            )
        )
    }

}

@Preview(showBackground = true, showSystemUi = true, name = "Playing")
@Composable
fun BingoGamePlayScreenPreviewPlaying() {
    val game = Game(
        id = 1,
        name = "Family Bingo Night",
        targetFigure = WinCondition.B,
        createdAt = "2024-06-01T12:00:00Z",
        isCompleted = false
    )
    val card = BingoCard(
        id = 1,
        gameId = 1,
        grid = List(5) { row ->
            List(5) { col ->
                val number = if (row == 2 && col == 2) null else (row * 15 + col + 1)
                BingoCell(
                    number = number,
                    isMarked = false,
                    isFree = (row == 2 && col == 2)
                )
            }
        }
    )
    val markedNumbers = setOf(3, 7, 12, 18)
    val gameState = GameState(
        game = game,
        card = card,
        markedNumbers = markedNumbers,
        isWon = false
    )
    FlexAppTheme {
        BingoGamePlayScreen(
            onNavigate = {},
            onEvent = {},
            state = BingoGamePlayScreenState(
                isLoading = false,
                gameState = gameState
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Win Dialog")
@Composable
fun BingoGamePlayScreenPreviewWinDialog() {
    val game = Game(
        id = 1,
        name = "Family Bingo Night",
        targetFigure = WinCondition.FULL_CARD,
        createdAt = "2024-06-01T12:00:00Z",
        isCompleted = true
    )
    val card = BingoCard(
        id = 1,
        gameId = 1,
        grid = List(5) { row ->
            List(5) { col ->
                BingoCell(
                    number = if (row == 2 && col == 2) null else (row * 15 + col + 1),
                    isMarked = true,
                    isFree = (row == 2 && col == 2)
                )
            }
        }
    )
    val markedNumbers = (1..75).toSet()
    val gameState = GameState(
        game = game,
        card = card,
        markedNumbers = markedNumbers,
        isWon = true
    )
    FlexAppTheme {
        BingoGamePlayScreen(
            onNavigate = {},
            onEvent = {},
            state = BingoGamePlayScreenState(
                isLoading = false,
                gameState = gameState,
                showWinDialog = true
            )
        )
    }
}
