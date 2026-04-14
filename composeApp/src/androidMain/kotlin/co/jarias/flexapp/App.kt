package co.jarias.flexapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import co.jarias.flexapp.ui.navigation.AppNavigation

@Composable
@Preview
fun App() {
    MaterialTheme {
        AppNavigation()
    }
}