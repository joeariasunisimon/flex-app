package co.jarias.flexapp.ui.screens.bingo.game_setup

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
import co.jarias.flexapp.ui.navigation.NavigationEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoGameSetupScreen(
    onNavigate: (NavigationEvent) -> Unit,
    onEvent: (BingoGameSetupScreenEvents) -> Unit,
    state: BingoGameSetupScreenState
) {
    LaunchedEffect(state.createdGameId) {
        state.createdGameId?.let { gameId ->
            onNavigate(NavigationEvent.NavigateToBingoCardSetup(gameId))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Create New Game") },
            navigationIcon = {
                IconButton(onClick = { onNavigate(NavigationEvent.OnNavigateUp) }) {
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
            Text(
                text = "Game Name",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = state.gameName,
                onValueChange = { onEvent(BingoGameSetupScreenEvents.OnGameNameChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                placeholder = { Text("Enter game name") },
                singleLine = true
            )

            Text(
                text = "You will set up your own Bingo card manually after creating the game.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            state.errorMessage?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { onEvent(BingoGameSetupScreenEvents.OnCreateGameClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
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
