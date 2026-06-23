# AGENTS.md — FlexApp Bingo Tracker

## Build & Run

```bash
./gradlew :composeApp:assembleDebug          # Android debug APK
./gradlew :shared:assembleSharedDebugFrameworkIosSimulatorArm64  # iOS framework (simulator)
./gradlew build                               # Full build (all modules)
./gradlew :shared:check                       # Run shared tests
./gradlew generateCommonMainBingoDatabaseInterface  # Regenerate SQLDelight after .sq changes
```

iOS app is opened from `iosApp/` in Xcode; the Kotlin framework must be built first.

No linter, formatter, or CI is configured in this repo.

## Architecture

Kotlin Multiplatform (KMP) with three modules:

| Module       | Purpose                                    | Entrypoint                                |
|-------------|-------------------------------------------|------------------------------------------|
| `:shared`   | Domain, data, DI (common + platform)      | `di/Koin.kt` — `initKoin()`             |
| `:composeApp` | Android UI (Jetpack Compose)            | `App.kt` → `NavHost`                    |
| `iosApp/`   | iOS UI (SwiftUI)                         | `iOSApp.swift` → `AppNavCoordinator`     |

- **State**: Android uses `ViewModel` + `StateFlow`; iOS uses `ObservableObject` + `@Published`, consuming Kotlin `Flow` via `FlowWatcher` from `shared`.
- **DI**: Koin Multiplatform. Shared module declares an `expect val platformModule`. Android's concrete module is in `composeApp/.../di/AppModule.kt`. iOS calls `KoinHelperKt.doInitKoin()`.
- **Persistence**: SQLDelight (SQLite) + DataStore (preferences).
- **Namespace**: `co.jarias.flexapp` (composeApp), `co.jarias.flexapp.shared` (shared).

## Screen Pattern (Mirrored Across Platforms)

Every screen follows the same 4-file pattern:

```
screen_folder/
├── {Screen}Screen.kt / .swift       # UI + Previews
├── {Screen}ScreenState.kt / .swift  # Data class / struct
├── {Screen}ScreenEvents.kt / .swift # Sealed class / enum for user actions
└── {Screen}ScreenViewModel.kt / .swift # ViewModel / ObservableObject
```

When adding a new screen, create all four files and register the ViewModel in `AppModule.kt` (Android) so Koin can inject it.

## Domain Model (Critical)

- **`WinCondition` is a sealed class**, not an enum. It serializes to/from JSON (not enum ordinal names). Always use `WinCondition.serialize()` / `WinCondition.deserialize()`. The DB column `target_figure` stores serialized JSON.
- **Game**: `id` is database-generated. After `insertGame`, capture the ID and return `game.copy(id = gameId)`. Never rely on the Game returned from the DB call to have a non-null `id`.
- **BingoCard grid**: 5x5 matrix. Column ranges: B=1–15, I=16–30, N=31–45, G=46–60, O=61–75. Center cell (row 2, col 2) is FREE (`number = null`, `isFree = true`).

## Database (SQLDelight)

- Schema: `shared/src/commonMain/sqldelight/.../BingoDatabase.sq`
- Database name: `BingoDatabase` → generates task `generateCommonMainBingoDatabaseInterface`
- Generated package: `co.jarias.flexapp.shared.database`
- **Never edit generated files** — only modify the `.sq` file.

## Patterns to Follow

1. **Always capture DB-generated IDs**: After `insertGame`, query the last row and return the ID. See `GameRepositoryImpl.insertGame()` and `CreateGameUseCase.invoke()`.
2. **Defensive deserialization**: When reading `target_figure` from the DB, wrap deserialization in try-catch and return `null` on failure — stale JSON can survive schema migrations.
3. **Camera previews**: Use `LocalInspectionMode.current` guard in `@Preview` composables to avoid camera initialization crashes at preview time.

## iOS Interop

- Framework name: `shared` (set by `baseName = "shared"` in shared/build.gradle.kts)
- Requires `linkerOpts("-lsqlite3")` in shared/build.gradle.kts and `libsqlite3.tbd` in Xcode
- Initialize Koin: `KoinHelperKt.doInitKoin()` in `iOSApp.swift`
- Consume Kotlin `Flow`: use `FlowWatcher<T>` from `shared/src/iosMain/.../util/FlowHelper.kt`
