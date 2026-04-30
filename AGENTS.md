# AGENTS.md - FlexApp Bingo Tracker

## Build Commands

```bash
# Android debug APK
./gradlew :composeApp:assembleDebug

# iOS framework (for development)
./gradlew :shared:assembleSharedDebugFrameworkIosSimulatorArm64

# Full build
./gradlew build

# Clean + build
./gradlew clean build

# Regenerate SQLDelight (after .sq changes)
./gradlew generateCommonMainBingoDatabaseInterface
```

## Project Structure

```
FlexApp/
├── shared/                    # Business logic (KMP shared)
│   └── src/commonMain/kotlin/co/jarias/flexapp/
│       ├── domain/model.kt    # Game, BingoCard, WinCondition
│       ├── domain/usecase/    # CreateGameUseCase, etc.
│       └── data/repository/   # GameRepository, etc.
├── composeApp/                 # Android UI (Jetpack Compose)
│   └── src/androidMain/kotlin/co/jarias/flexapp/
│       ├── ui/
│       │   ├── navigation/    # AppDestinations, NavigationEvent, AppNavigation
│       │   └── screens/
│       │       ├── welcome/
│       │       ├── tools/
│       │       └── bingo/
│       │           ├── card_setup/
│       │           ├── figure_selection/
│       │           ├── game_list/
│       │           ├── game_play/
│       │           └── game_setup/
│       └── App.kt
└── iosApp/                    # iOS entry point (SwiftUI)
```

## Architecture

- **Clean Architecture** with Domain, Data layers
- **SQLDelight** for SQLite persistence
- **Kotlin Multiplatform** targeting Android + iOS
- Platform-specific `DatabaseDriverFactory` via expect/actual pattern

## Screen Pattern

Each screen follows this pattern with files in its own subfolder:
```
screen_folder/
├── {Screen}Screen.kt         # Composable UI
├── {Screen}ScreenEvents.kt  # Sealed class for events
├── {Screen}ScreenState.kt   # Data class for state
└── {Screen}ScreenViewModel.kt # ViewModel handling business logic
```

### Screen Signature Pattern
```kotlin
@Composable
fun ScreenName(
    onNavigate: (NavigationEvent) -> Unit,
    onEvent: (ScreenNameScreenEvents) -> Unit,
    state: ScreenNameScreenState
)
```

## Navigation Structure

### AppDestinations.kt
Route definitions using object pattern:
```kotlin
object AppDestinations {
    const val WELCOME = "welcome"
    // ...
    fun bingoGamePlayRoute(gameId: Long) = "bingo_game_play/$gameId"
}
```

### NavigationEvent.kt
Navigation events sealed class:
```kotlin
sealed class NavigationEvent {
    data object OnNavigateUp : NavigationEvent()
    data object NavigateToBingoGameList : NavigationEvent()
    data class NavigateToBingoGamePlay(val gameId: Long) : NavigationEvent()
}
```

### AppNavigation.kt
NavHost setup with ViewModels created per screen using `remember`.

## Database (SQLDelight)

- Schema file: `shared/src/commonMain/sqldelight/.../BingoDatabase.sq`
- Generated code location: `build/generated/sqldelight/...`
- **Do not edit generated files** - modify `.sq` files instead

## Critical Bug Fixes (important context)

1. **WinCondition handling** - `GameRepositoryImpl.kt` uses try-catch for enum deserialization. Invalid values return `null` instead of crashing.

2. **CreateGameUseCase** - Must return `game.copy(id = gameId)` after insert. Previously returned game with `id = null`, causing UI hang.

3. **target_figure column** - Nullable in current schema. Clear app data if NOT NULL errors occur.

## Key File Locations

| Component | Path |
|-----------|------|
| Domain models | `shared/.../domain/model.kt` |
| Create game logic | `shared/.../domain/usecase/CreateGameUseCase.kt` |
| Database queries | `shared/.../sqldelight/.../BingoDatabase.sq` |
| Navigation setup | `composeApp/.../ui/navigation/AppNavigation.kt` |
| Navigation events | `composeApp/.../ui/navigation/NavigationEvent.kt` |
| Screen destinations | `composeApp/.../ui/navigation/AppDestinations.kt` |
| Screen components | `composeApp/.../ui/screens/{feature}/{Screen}*.kt` |

## WinCondition Enum Values

Currently supported: `B`, `I`, `N`, `G`, `O`, `FULL_CARD`

Do NOT use: `FIVE_IN_ROW`, `X`, `FULL` (these are obsolete)

## Dependencies (libs.versions.toml)

- Kotlin 2.3.20
- Compose Multiplatform 1.10.3
- SQLDelight 2.0.2
- Android minSdk 29, targetSdk 36

## Testing

- No dedicated test suite yet
- Manual testing via `BingoGameSetupScreen` creates games end-to-end
- Test new features by running `:composeApp:assembleDebug` and installing APK
