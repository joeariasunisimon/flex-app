package co.jarias.flexapp.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import co.jarias.flexapp.ui.navigation.NavigationEvent
import co.jarias.flexapp.R
import co.jarias.flexapp.ui.theme.FlexAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolSelectionScreen(
    onNavigate: (NavigationEvent) -> Unit,
    onEvent: (ToolSelectionScreenEvents) -> Unit,
    state: ToolSelectionScreenState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Select a Game",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
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
                    iconRes = R.drawable.bingo_icon,
                    onClick = {
                        onEvent(ToolSelectionScreenEvents.OnBingoSelected)
                        onNavigate(NavigationEvent.NavigateToBingoGameList)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ToolCard(
                    title = "Coming Soon...",
                    description = "More exciting games will be available soon!",
                    iconRes = R.drawable.rocket_icon,
                    onClick = { },
                    enabled = false
                )
            }
        }
    }
}

@Composable
private fun ToolCard(
    title: String,
    description: String,
    iconRes: Int,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = 0.5f
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 16.dp)
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

@Preview(
    showBackground = true,
    showSystemUi = true,
)
@Composable
fun ToolSelectionScreenPreview() {
    FlexAppTheme {
        ToolSelectionScreen(
            onNavigate = {},
            onEvent = {},
            state = ToolSelectionScreenState()
        )
    }
}