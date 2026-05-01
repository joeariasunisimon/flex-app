package co.jarias.flexapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import co.jarias.flexapp.ui.navigation.AppNavigation
import co.jarias.flexapp.ui.theme.FlexAppTheme

@Composable
fun App() {
    FlexAppTheme {
        val navController = rememberNavController()
        AppNavigation(navController = navController)
    }
}
