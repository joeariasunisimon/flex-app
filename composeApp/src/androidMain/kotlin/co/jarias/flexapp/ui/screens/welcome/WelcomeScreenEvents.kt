package co.jarias.flexapp.ui.screens.welcome

sealed class WelcomeScreenEvents {
    data object OnGetStartedClicked : WelcomeScreenEvents()
}
