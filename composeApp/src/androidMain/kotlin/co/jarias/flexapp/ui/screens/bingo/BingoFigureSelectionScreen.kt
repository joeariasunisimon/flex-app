package co.jarias.flexapp.ui.screens.bingo

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jarias.flexapp.data.local.Database
import co.jarias.flexapp.data.local.DatabaseDriverFactory
import co.jarias.flexapp.data.repository.BingoCardRepositoryImpl
import co.jarias.flexapp.data.repository.GameRepositoryImpl
import co.jarias.flexapp.domain.BingoCell
import co.jarias.flexapp.domain.WinCondition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoFigureSelectionScreen(
    gameId: Long,
    onFigureSelected: (WinCondition) -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    var selectedFigure by remember { mutableStateOf<WinCondition?>(null) }
    var cardGrid by remember { mutableStateOf<List<List<BingoCell>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isUpdating by remember { mutableStateOf(false) }
    var customPattern by remember { mutableStateOf<Set<Pair<Int, Int>>>(emptySet()) }

    val database = remember {
        Database(DatabaseDriverFactory(context))
    }
    val bingoCardRepository = remember {
        BingoCardRepositoryImpl(database)
    }
    val gameRepository = remember {
        GameRepositoryImpl(database)
    }

    LaunchedEffect(gameId) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val cards = withContext(Dispatchers.IO) {
                    bingoCardRepository.getCardsByGameId(gameId)
                }
                if (cards.isNotEmpty()) {
                    cardGrid = cards.first().grid
                }
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                // Handle error
            }
        }
    }

    val columnLabels = listOf("B", "I", "N", "G", "O")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Select Win Figure") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Text("←", style = MaterialTheme.typography.headlineSmall)
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

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                // Bingo Card with visual feedback
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
                        // Column headers
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

                        // Grid rows
                        for (row in 0..4) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.17f),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (col in 0..4) {
                                    val cell = cardGrid[row][col]
                                    val isFree = cell.isFree
                                    val isCustomSelected = customPattern.contains(Pair(row, col))
                                    val isHighlighted = selectedFigure?.requiredCells?.contains(Pair(row, col)) == true || isCustomSelected
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
                                                // Toggle custom pattern selection
                                                val pos = Pair(row, col)
                                                customPattern = if (customPattern.contains(pos)) {
                                                    customPattern - pos
                                                } else {
                                                    customPattern + pos
                                                }
                                                selectedFigure = null // Clear quick selection if drawing
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cell.number?.toString() ?: "FREE",
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

            // Info about selected figure or custom pattern
            if (selectedFigure != null || customPattern.isNotEmpty()) {
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
                        if (selectedFigure != null) {
                            Text(
                                text = "Selected: ${selectedFigure!!.displayName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You need to mark ${selectedFigure!!.requiredCells.size} cells to win!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else if (customPattern.isNotEmpty()) {
                            Text(
                                text = "Custom Pattern Selected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You need to mark ${customPattern.size} cells to win!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick select columns (B, I, N, G, O)
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
                        onClick = {
                            selectedFigure = colCond
                            customPattern = emptySet()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedFigure == colCond) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Text(colCond.displayName)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Full Card Button
            OutlinedButton(
                onClick = {
                    selectedFigure = WinCondition.FULL_CARD
                    customPattern = emptySet()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedFigure == WinCondition.FULL_CARD) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) {
                Text("Or select Full Card (all 25 cells)")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue Button
            Button(
                onClick = {
                    if (selectedFigure != null || customPattern.isNotEmpty()) {
                        isUpdating = true
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val game = withContext(Dispatchers.IO) {
                                    gameRepository.getGameById(gameId)
                                }
                                val winCondition = selectedFigure ?: WinCondition.Custom(requiredCells = customPattern)
                                if (game != null) {
                                    val updatedGame = game.copy(targetFigure = winCondition)
                                    withContext(Dispatchers.IO) {
                                        gameRepository.updateGame(updatedGame)
                                    }
                                }
                                isUpdating = false
                                onFigureSelected(winCondition)
                            } catch (e: Exception) {
                                isUpdating = false
                                // Handle error
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = (selectedFigure != null || customPattern.isNotEmpty()) && !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
