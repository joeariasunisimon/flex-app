package co.jarias.flexapp.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.jarias.flexapp.ui.navigation.NavigationEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolSelectionScreen(
    onNavigate: (NavigationEvent) -> Unit,
    onEvent: (ToolSelectionScreenEvents) -> Unit,
    state: ToolSelectionScreenState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
TopAppBar(
                title = { Text("Select a Game") },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(NavigationEvent.OnNavigateUp) }) {
                        Text("←", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Choose a game to play",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            ToolCard(
                title = "Bingo",
                description = "Play the classic bingo game with customizable cards and target figures",
                icon = "🎲",
                onClick = { onNavigate(NavigationEvent.NavigateToBingoGameList) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ToolCard(
                title = "Coming Soon...",
                description = "More exciting games will be available soon!",
                icon = "🚀",
                onClick = { },
                enabled = false
            )
        }
    }
}

@Composable
private fun ToolCard(
    title: String,
    description: String,
    icon: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
