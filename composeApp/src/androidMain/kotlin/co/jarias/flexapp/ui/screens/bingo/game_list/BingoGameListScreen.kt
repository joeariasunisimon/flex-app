package co.jarias.flexapp.ui.screens.bingo.game_list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jarias.flexapp.R
import co.jarias.flexapp.domain.Game
import co.jarias.flexapp.domain.WinCondition
import co.jarias.flexapp.ui.navigation.NavigationEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoGameListScreen(
    onNavigate: (NavigationEvent) -> Unit,
    onEvent: (BingoGameListScreenEvents) -> Unit,
    state: BingoGameListScreenState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Bingo Games") },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(NavigationEvent.OnNavigateUp) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Button(onClick = { onNavigate(NavigationEvent.NavigateToBingoGameSetup) }) {
                        Text("New Game")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
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
                        Button(onClick = { onEvent(BingoGameListScreenEvents.OnRetryClicked) }) {
                            Text("Retry")
                        }
                    }
                }

                state.games.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No games yet",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create your first Bingo game to get started!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { onNavigate(NavigationEvent.NavigateToBingoGameSetup) }) {
                            Text("Create Game")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.games) { game ->
                            GameListItem(
                                game = game,
                                pendingSetupGameIds = state.pendingSetupGameIds,
                                onContinueSetup = {
                                    game.id?.let {
                                        onEvent(BingoGameListScreenEvents.OnContinueSetup(it))
                                    }
                                },
                                onPlay = {
                                    game.id?.let {
                                        onNavigate(
                                            NavigationEvent.NavigateToBingoGamePlay(
                                                it
                                            )
                                        )
                                    }
                                },
                                onRestart = {
                                    game.id?.let {
                                        onEvent(
                                            BingoGameListScreenEvents.OnRestartGame(
                                                it
                                            )
                                        )
                                    }
                                },
                                onDelete = {
                                    game.id?.let { onEvent(BingoGameListScreenEvents.OnDeleteGame(it)) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameListItem(
    game: Game,
    pendingSetupGameIds: List<Long>,
    onContinueSetup: () -> Unit,
    onPlay: () -> Unit,
    onRestart: () -> Unit,
    onDelete: () -> Unit
) {
    val isPendingSetup = pendingSetupGameIds.contains(game.id) || game.targetFigure == null
    val isReadyToPlay = !game.isCompleted && game.targetFigure != null
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Delete Game?") },
            text = { Text("Are you sure you want to delete \"${game.name}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = if (isPendingSetup) onContinueSetup else onPlay),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val statusText = when {
                        isPendingSetup -> "Setup incomplete"
                        game.isCompleted -> "Game finished"
                        else -> "In progress • Target: ${game.targetFigure?.displayName ?: "None"}"
                    }
                    
                    val statusColor = when {
                        isPendingSetup -> MaterialTheme.colorScheme.error
                        game.isCompleted -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor
                    )
                }
                if (game.isCompleted) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "✅",
                            modifier = Modifier.padding(8.dp),
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Text(
                text = "Created: ${game.createdAt.take(10)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isPendingSetup) {
                    Button(
                        onClick = onContinueSetup,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("Continue Setup", fontSize = 12.sp)
                    }
                } else if (isReadyToPlay) {
                    OutlinedButton(
                        onClick = onRestart,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_change_circle_24),
                            contentDescription = "Restart",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restart", fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    contentPadding = PaddingValues(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.outline_delete_24),
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Empty State"
)
@Composable
fun BingoGameListScreenPreviewEmpty() {
    BingoGameListScreen(
        onNavigate = {},
        onEvent = {},
        state = BingoGameListScreenState(isLoading = false)
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "With Games"
)
@Composable
fun BingoGameListScreenPreviewGames() {
    BingoGameListScreen(
        onNavigate = {},
        onEvent = {},
        state = BingoGameListScreenState(
            isLoading = false,
            games = listOf(
                Game(
                    id = 1,
                    name = "Family Bingo Night",
                    targetFigure = WinCondition.FULL_CARD,
                    isCompleted = false,
                    createdAt = "2024-06-01T12:00:00Z"
                ),
                Game(
                    id = 2,
                    name = "Office Bingo Challenge",
                    targetFigure = WinCondition.B,
                    isCompleted = true,
                    createdAt = "2024-05-28T18:30:00Z"
                )
            )
        )
    )
}