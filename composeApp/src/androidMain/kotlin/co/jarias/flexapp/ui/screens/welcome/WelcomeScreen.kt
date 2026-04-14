package co.jarias.flexapp.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .safeContentPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "FlexApp",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Subtitle
        Text(
            text = "Your Multi-Purpose Gaming Companion",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 48.dp),
            textAlign = TextAlign.Center
        )

        // Description
        Text(
            text = "Experience the joy of classic games with modern technology. Bingo, and more games to come!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 48.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // Get Started Button
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
