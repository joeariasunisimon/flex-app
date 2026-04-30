package co.jarias.flexapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import co.jarias.flexapp.ui.navigation.AppNavigation

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        AppNavigation(navController = navController)
    }
}
