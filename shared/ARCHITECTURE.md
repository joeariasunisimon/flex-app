# Bingo Tracker - Shared Module Architecture

## Overview

The shared module implements **Clean Architecture** using Kotlin Multiplatform (KMP) to centralize business logic while allowing platform-native UIs (Jetpack Compose for Android, SwiftUI for iOS).

## Architecture Layers

### 1. **Domain Layer** (`domain/`)
Pure business logic independent of any framework or platform.

**Components:**
- **Models** (`model.kt`):
  - `Game` - Represents a bingo game instance with completion status
  - `BingoCard` - Contains 5x5 bingo card grid with cells
  - `BingoCell` - Individual cell with number, marked status, and free space flag
  - `MarkedNumber` - Tracks marked numbers during gameplay
  - `WinCondition` - Enum defining different win patterns (5-in-row, X, Full card, N-shape)
  - `GameState` - Complete game state for gameplay

- **Use Cases** (`usecase/`):
  - `CreateGameUseCase` - Creates a new game with win condition
  - `GenerateBingoCardUseCase` - Generates random 5x5 bingo card
  - `MarkNumberUseCase` - Marks a number as called
  - `CheckWinConditionUseCase` - Checks if win condition is met
  - `CompleteGameUseCase` - Marks game as completed
  - `GetGameStateUseCase` - Retrieves complete game state

### 2. **Data Layer** (`data/`)

#### **Local Data Source** (`local/`)
Handles platform-specific database driver initialization:

- **`DatabaseDriverFactory.kt`** (expect/actual):
  - Common interface for creating SQLite drivers
  - **Android implementation**: Uses `AndroidSqliteDriver`
  - **iOS implementation**: Uses `NativeSqliteDriver`

- **`Database.kt`**:
  - Initializes the SQLDelight database
  - Provides access to all query objects

#### **Repositories** (`repository/`)
Implements the repository pattern for data access:

- **`GameRepository` & `GameRepositoryImpl`**
  ```kotlin
  suspend fun getAllGames(): List<Game>
  suspend fun insertGame(game: Game): Long
  suspend fun updateGame(game: Game)
  suspend fun deleteGame(gameId: Long)
  suspend fun getGameById(gameId: Long): Game?
  ```

- **`BingoCardRepository` & `BingoCardRepositoryImpl`**
  ```kotlin
  suspend fun getCardsByGameId(gameId: Long): List<BingoCard>
  suspend fun insertCard(card: BingoCard)
  suspend fun deleteCard(cardId: Long)
  ```

- **`MarkedNumberRepository` & `MarkedNumberRepositoryImpl`**
  ```kotlin
  suspend fun getMarkedNumbersByGameId(gameId: Long): List<MarkedNumber>
  suspend fun insertMarkedNumber(markedNumber: MarkedNumber)
  suspend fun deleteMarkedNumber(markedNumberId: Long)
  suspend fun clearMarkedNumbersForGame(gameId: Long)
  ```

## Database Schema

### SQLDelight Configuration
- **Package**: `co.jarias.flexapp.shared.database`
- **File**: `shared/src/commonMain/sqldelight/co/jarias/flexapp/shared/database/BingoDatabase.sq`

### Tables

#### **Game**
| Column | Type | Constraints |
|--------|------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| name | TEXT | NOT NULL |
| target_figure | TEXT | NOT NULL |
| created_at | TEXT | NOT NULL |
| is_completed | INTEGER | NOT NULL DEFAULT 0 |
| completed_at | TEXT |

#### **BingoCard**
| Column | Type | Constraints |
|--------|------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| game_id | INTEGER | NOT NULL, FK → Game(id) |
| grid_data | TEXT | NOT NULL (JSON serialized 5x5 grid) |

#### **MarkedNumber**
| Column | Type | Constraints |
|--------|------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| game_id | INTEGER | NOT NULL, FK → Game(id) |
| number | INTEGER | NOT NULL |

**Note**: BingoCard grid is stored as JSON and deserialized to `List<List<BingoCell>>` for proper 5x5 bingo card representation with FREE space support.

## Directory Structure

```
shared/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/co/jarias/flexapp/
│   │   │   ├── domain/
│   │   │   │   ├── model.kt
│   │   │   │   └── usecase/
│   │   │   │       └── CreateGameUseCase.kt
│   │   │   └── data/
│   │   │       ├── local/
│   │   │       │   ├── DatabaseDriverFactory.kt (expect)
│   │   │       │   └── Database.kt
│   │   │       └── repository/
│   │   │           ├── GameRepository.kt
│   │   │           ├── GameRepositoryImpl.kt
│   │   │           ├── BingoCardRepository.kt
│   │   │           ├── BingoCardRepositoryImpl.kt
│   │   │           ├── MarkedNumberRepository.kt
│   │   │           └── MarkedNumberRepositoryImpl.kt
│   │   └── sqldelight/co/jarias/flexapp/shared/database/
│   │       └── BingoDatabase.sq
│   ├── androidMain/
│   │   └── kotlin/co/jarias/flexapp/data/local/
│   │       └── DatabaseDriverFactory.kt (actual)
│   └── iosMain/
│       └── kotlin/co/jarias/flexapp/data/local/
│           └── DatabaseDriverFactory.kt (actual)
└── build.gradle.kts
```

## Dependencies

### Core
- **SQLDelight**: 2.0.2
  - `app.cash.sqldelight:runtime`
  - `app.cash.sqldelight:coroutines-extensions`
  - Platform-specific drivers (Android & Native)

### Utilities
- **kotlinx-datetime**: 0.6.1 (for timestamp management)

## Usage Examples

### Creating a Game
```kotlin
val createGameUseCase = CreateGameUseCase(gameRepository)
val newGame = createGameUseCase("My Game", "5 in a row")
```

### Retrieving All Games
```kotlin
val games = gameRepository.getAllGames()
```

### Managing Bingo Cards
```kotlin
val card = BingoCard(gameId = 1, numbers = listOf(1, 2, 3, 4, 5, ...))
bingoCardRepository.insertCard(card)

val gameCards = bingoCardRepository.getCardsByGameId(gameId = 1)
```

### Tracking Marked Numbers
```kotlin
val markedNum = MarkedNumber(gameId = 1, number = 42)
markedNumberRepository.insertMarkedNumber(markedNum)

val marked = markedNumberRepository.getMarkedNumbersByGameId(gameId = 1)
```

### Restarting a Game
```kotlin
markedNumberRepository.clearMarkedNumbersForGame(gameId = 1)
```

### Dropping a Game
```kotlin
gameRepository.deleteGame(gameId = 1) // Cascades to cards and marked numbers
```

## Data Flow

```
UI Layer (Android/iOS)
        ↓
Use Cases (Domain)
        ↓
Repositories (Data)
        ↓
SQLDelight Queries
        ↓
SQLite Database
```

## Future Enhancements

1. **Additional Use Cases**:
   - `MarkNumberUseCase` - Mark a number as called
   - `CheckWinConditionUseCase` - Verify if target figure is complete
   - `GetGameStateUseCase` - Retrieve complete game state

2. **Reactive Data Streams**:
   - Convert repositories to return `Flow<T>` for reactive UI updates
   - Implement `collectAsState()` in Compose and SwiftUI

3. **Input Validation**:
   - Add validation layer for game names and figure targets
   - Validate number ranges (1-90 for standard bingo)

4. **Testing**:
   - Unit tests for repositories and use cases
   - Integration tests with in-memory SQLDelight driver

5. **Error Handling**:
   - Result wrapper type for better error propagation
   - Custom exceptions for business rule violations

6. **Performance**:
   - Pagination for large game lists
   - Caching strategies for frequently accessed games

## Platform Implementation Notes

### Android
- Database driver: `AndroidSqliteDriver` from SQLDelight
- Database file: `bingo.db` stored in app's private files directory
- Context required for initialization

### iOS
- Database driver: `NativeSqliteDriver` (native SQLite)
- Database file: `bingo.db` stored in app's documents directory
- No additional dependencies required

## Configuration

### Gradle Setup
```kotlin
sqldelight {
    databases {
        create("BingoDatabase") {
            packageName.set("co.jarias.flexapp.shared.database")
        }
    }
}
```

### Kotlin Compiler Flag (Optional)
To suppress expect/actual class warnings, add to `build.gradle.kts`:
```kotlin
compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
}
```

---

**Last Updated**: April 12, 2026
**Architecture Pattern**: Clean Architecture + Repository Pattern + Multiplatform

