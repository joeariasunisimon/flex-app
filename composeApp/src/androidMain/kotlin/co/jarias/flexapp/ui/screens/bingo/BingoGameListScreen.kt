package co.jarias.flexapp.ui.screens.bingo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jarias.flexapp.viewmodel.BingoGameListViewModel
import co.jarias.flexapp.data.local.DatabaseDriverFactory
import co.jarias.flexapp.data.local.Database
import co.jarias.flexapp.data.repository.GameRepositoryImpl
import co.jarias.flexapp.data.repository.BingoCardRepositoryImpl
import co.jarias.flexapp.data.repository.MarkedNumberRepositoryImpl
import androidx.compose.ui.platform.LocalContext
import co.jarias.flexapp.domain.Game
import co.jarias.flexapp.domain.usecase.RestartGameUseCase
import co.jarias.flexapp.domain.usecase.DropGameUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoGameListScreen(
    onGamePlay: (Long) -> Unit,
    onGameSetup: (Long) -> Unit,
    onCreateNewGame: () -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current

    // Create repositories
    val database = remember {
        Database(DatabaseDriverFactory(context))
    }
    val gameRepository = remember {
        GameRepositoryImpl(database)
    }
    val bingoCardRepository = remember {
        BingoCardRepositoryImpl(database)
    }
    val markedNumberRepository = remember {
        MarkedNumberRepositoryImpl(database)
    }
    val viewModel: BingoGameListViewModel = remember {
        BingoGameListViewModel(gameRepository)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Bingo Games") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Text("←", fontSize = 20.sp)
                    }
                },
                actions = {
                    Button(onClick = onCreateNewGame) {
                        Text("New Game")
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
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadGames() }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.games.isEmpty() -> {
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
                        Button(onClick = onCreateNewGame) {
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
                        items(uiState.games) { game ->
                            GameListItem(
                                game = game,
                                onPlay = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        val hasCard = withContext(Dispatchers.IO) {
                                            bingoCardRepository.getCardByGameId(game.id!!) != null
                                        }
                                        if (hasCard) {
                                            onGamePlay(game.id!!)
                                        } else {
                                            onGameSetup(game.id!!)
                                        }
                                    }
                                },
                                onRestart = {
                                    val restartUseCase = RestartGameUseCase(gameRepository, markedNumberRepository)
                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                restartUseCase(game.id!!)
                                            }
                                            viewModel.loadGames()
                                        } catch (_: Exception) {
                                            // Handle error
                                        }
                                    }
                                },
                                onDelete = {
                                    val dropUseCase = DropGameUseCase(gameRepository, bingoCardRepository, markedNumberRepository)
                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                dropUseCase(game.id!!)
                                            }
                                            viewModel.loadGames()
                                        } catch (_: Exception) {
                                            // Handle error
                                        }
                                    }
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
    onPlay: () -> Unit,
    onRestart: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Game?") },
            text = { Text("Are you sure you want to delete \"${game.name}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
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
            .clickable(onClick = onPlay),
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
                    Text(
                        text = "Target: ${game.targetFigure?.displayName ?: "Not set"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
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

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("↻ Restart", fontSize = 12.sp)
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
                    Text("🗑 Delete", fontSize = 12.sp)
                }
            }
        }
    }
}
