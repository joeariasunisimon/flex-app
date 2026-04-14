package co.jarias.flexapp.ui.screens.bingo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.jarias.flexapp.viewmodel.BingoGameViewModel
import co.jarias.flexapp.data.local.DatabaseDriverFactory
import co.jarias.flexapp.data.local.Database
import co.jarias.flexapp.data.repository.GameRepositoryImpl
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoGameSetupScreen(
    onGameCreated: (Long) -> Unit,
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
    val viewModel: BingoGameViewModel = remember {
        BingoGameViewModel(gameRepository)
    }

    val uiState by viewModel.uiState.collectAsState()

    // Navigate on game creation
    LaunchedEffect(uiState.gameCreated) {
        if (uiState.gameCreated && uiState.createdGameId != null) {
            onGameCreated(uiState.createdGameId!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Create New Game") },
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
                .padding(24.dp)
        ) {
            // Game Name Input
            Text(
                text = "Game Name",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = uiState.gameName,
                onValueChange = { viewModel.updateGameName(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                placeholder = { Text("Enter game name") },
                singleLine = true
            )

            // Info about card generation
            Text(
                text = "You will set up your own Bingo card manually after creating the game.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Error Message
            if (uiState.errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Create Game Button
            Button(
                onClick = { viewModel.createGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Game", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
