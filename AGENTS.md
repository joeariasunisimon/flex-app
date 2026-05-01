# AGENTS.md - FlexApp Bingo Tracker

## Build Commands

```bash
# Android debug APK
./gradlew :composeApp:assembleDebug

# iOS framework (for development - simulator)
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
│   ├── src/commonMain/kotlin/co/jarias/flexapp/
│   │   ├── domain/model.kt    # Game, BingoCard, WinCondition (Sealed)
│   │   ├── domain/usecase/    # CreateGameUseCase, etc.
│   │   └── data/repository/   # GameRepository, etc.
│   └── src/iosMain/kotlin/co/jarias/flexapp/
│       ├── di/KoinHelper.kt   # Swift-friendly Koin entry point
│       └── util/FlowHelper.kt # FlowWatcher for iOS consumption
├── composeApp/                 # Android UI (Jetpack Compose)
│   └── src/androidMain/kotlin/co/jarias/flexapp/
│       ├── ui/
│       │   ├── navigation/    # AppDestinations, NavigationEvent, AppNavigation
│       │   └── screens/
│       │       ├── welcome/
│       │       ├── tool_selection/
│       │       └── bingo/
│       │           ├── card_setup/
│       │           ├── card_scanner/
│       │           ├── figure_selection/
│       │           ├── game_list/
│       │           ├── game_play/
│       │           └── game_setup/
│       └── App.kt
└── iosApp/                    # iOS entry point (SwiftUI)
    └── iosApp/
        ├── NavCoordinators/   # AppNavCoordinator, NavigationEvent (Swift)
        └── Screens/           # Mirrored structure to Android
            ├── welcome/
            ├── tool_selection/
            └── bingo/
                ├── game_list/
                └── ...
```

## Architecture & Design Philosophy

- **Clean Architecture**: Domain, Data, and Presentation layers.
- **"Serious Play" Design**: Earthy teal (`#267365`) + amber (`#F29F05`) palette. DM Sans for typography.
- **State Management**: 
    - Android: `ViewModel` with `StateFlow`.
    - iOS: `ObservableObject` (ViewModel) with `@Published` state, using `FlowWatcher` to collect Kotlin Flows.
- **Dependency Injection**: Koin (Multiplatform).
- **Persistence**: SQLDelight (SQLite) for database, DataStore (KMP) for preferences.
- **Navigation**:
    - Android: Jetpack Compose Navigation with `NavHost`.
    - iOS: Coordinator Pattern using `NavigationStack` and `NavigationPath`.

## Screen Pattern (Mirrored)

Both platforms follow a similar pattern for screens:

### Android (Compose)
```
screen_folder/
├── {Screen}Screen.kt         # Composable UI + Previews
├── {Screen}ScreenEvents.kt  # Sealed class for events
├── {Screen}ScreenState.kt   # Data class for state
└── {Screen}ScreenViewModel.kt # ViewModel (Koin)
```

### iOS (SwiftUI)
```
screen_folder/
├── {Screen}ScreenView.swift     # SwiftUI View
├── {Screen}ScreenEvents.swift   # Enum for events
├── {Screen}ScreenState.swift    # Struct for state
└── {Screen}ScreenViewModel.swift # ObservableObject (Swift)
```

## Key Game Flows

### Game Creation & Setup
1. **Game Setup**: Create game with just a name.
2. **Card Setup**: Fill 5x5 grid (Columns: B 1-15, I 16-30, N 31-45, G 46-60, O 61-75). Supports step-by-step entry.
3. **Figure Selection**: Choose target win condition (B, I, N, G, O, or Full Card).
4. **Gameplay**: Mark numbers to match the target figure. Real-time progress tracking.

## Troubleshooting & Historical Bug Fixes

### Game Creation Hangs (Fixed)
- **Problem**: UI stayed in loading state after clicking "Create".
- **Root Cause**: `CreateGameUseCase` returned game with `id = null`.
- **Fix**: Captured the database-generated ID and returned `game.copy(id = gameId)`.

### Game List Crashes (Fixed)
- **Problem**: Crash with `No enum constant ... WinCondition.FIVE_IN_ROW`.
- **Root Cause**: Obsolete enum strings in the database.
- **Fix**: Updated `GameRepositoryImpl` with a try-catch to return `null` ("Not set") for invalid strings.

### SQLite Constraint Errors (Fixed)
- **Problem**: `NOT NULL constraint failed: Game.target_figure`.
- **Root Cause**: Outdated local database schema.
- **Fix**: Clear app data to force a fresh, correct schema creation.

## iOS Interop (Kotlin/Native)

- **Framework Name**: `shared` (lowercase).
- **Koin Initialization**: Done via `KoinHelperKt.doInitKoin()` in `iOSApp.swift`.
- **Flow Consumption**: Use `FlowWatcher<T>` from Swift to collect Kotlin `Flow` types.
- **Linking**: Requires `linkerOpts("-lsqlite3")` in `shared/build.gradle.kts` and `libsqlite3.tbd` in Xcode "Link Binary With Libraries".

## Database (SQLDelight)

- Schema file: `shared/src/commonMain/sqldelight/.../BingoDatabase.sq`
- **Do not edit generated files** - modify `.sq` files instead.

## Roadmap (Planned)

- [ ] **Phase 4**: Audio & Visual feedback (sound effects, vibrations, mark animations).
- [ ] **Phase 5**: Multi-card support (playing with more than one card per game).
- [ ] **Phase 6**: Custom ranges and rules customization.
- [ ] **Phase 7**: Social features (game sharing via JSON/QR, game statistics).

## Testing & Previews

- **Compose Previews**: Available in most screen files. Camera components use `LocalInspectionMode` check to avoid crashes.
- **iOS Previews**: Use `#Preview` macros with mock Coordinators.
