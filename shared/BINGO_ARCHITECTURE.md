# Bingo Game Architecture - Shared Module

## Overview
This document describes the clean architecture implementation for the Bingo tracker feature in the FlexApp shared module. The architecture is built with Kotlin Multiplatform (KMP) to support both Android (Jetpack Compose) and iOS (SwiftUI).

## Architecture Layers

### 1. Domain Layer
The core business logic, independent of any framework.

#### Models (`domain/model.kt`)
- **BingoCell**: Represents a single cell in the bingo card
  - `number: Int?` - The number in the cell (null for FREE space)
  - `isMarked: Boolean` - Whether this cell has been marked
  - `isFree: Boolean` - Whether this is the FREE space

- **BingoCard**: Represents the 5x5 bingo card
  - `id: Long?` - Database ID
  - `gameId: Long` - Associated game
  - `grid: List<List<BingoCell>>` - 5x5 grid of cells
  - Functions:
    - `generateRandomCard()` - Generates a random card (for development)
    - `createCardFromGrid(numbers)` - Creates a card from user input
    - `serializeGrid(grid)` - JSON serialization
    - `deserializeGrid(json)` - JSON deserialization

- **Game**: Represents a bingo game session
  - `id: Long?` - Database ID
  - `name: String` - Game name
  - `targetFigure: WinCondition?` - The winning pattern
  - `createdAt: String` - Creation timestamp
  - `isCompleted: Boolean` - Whether game is won
  - `completedAt: String?` - Win timestamp

- **MarkedNumber**: Tracks called numbers
  - `id: Long?` - Database ID
  - `gameId: Long` - Associated game
  - `number: Int` - The marked number (1-90)

- **WinCondition**: Enum for winning patterns
  - `B`, `I`, `N`, `G`, `O` - Full columns
  - `FULL_CARD` - All 25 cells
  - Each has `displayName` and `requiredCells: Set<Pair<Int, Int>>`

- **GameState**: Current game state snapshot
  - `game: Game` - Current game
  - `card: BingoCard` - Current card
  - `markedNumbers: Set<Int>` - All marked numbers
  - `isWon: Boolean` - Win status

#### Use Cases (`domain/usecase/`)

**Game Initialization:**
- `CreateGameUseCase` - Creates a new game with optional target figure
- `GetAllGamesUseCase` - Retrieves all games

**Card Management:**
- `GenerateBingoCardUseCase` - Generates and inserts a bingo card

**Game Play:**
- `MarkNumberUseCase` - Marks a number and validates it's not already marked
- `CheckWinConditionUseCase` - Checks if the target figure is complete
- `CompleteGameUseCase` - Marks game as won
- `GetGameStateUseCase` - Gets current game state

**Game Management:**
- `GetGameByIdUseCase` - Gets a specific game
- `RestartGameUseCase` - Resets marked numbers and completion status
- `DropGameUseCase` - Deletes game and all associated data
- `UpdateGameNameUseCase` - Updates game name

### 2. Data Layer
Handles data persistence and repository pattern.

#### Repositories (`data/repository/`)

**GameRepository**
- `getAllGames()` - Get all games
- `insertGame(game)` - Create new game
- `updateGame(game)` - Update game
- `updateGameCompletion(gameId, isCompleted, completedAt)` - Mark as won
- `deleteGame(gameId)` - Delete game
- `getGameById(gameId)` - Get specific game

**BingoCardRepository**
- `getCardsByGameId(gameId)` - Get card(s) for a game
- `insertCard(card)` - Save card
- `deleteCard(cardId)` - Delete card

**MarkedNumberRepository**
- `getMarkedNumbersByGameId(gameId)` - Get all marked numbers
- `insertMarkedNumber(markedNumber)` - Mark a number
- `deleteMarkedNumber(markedNumberId)` - Unmark a number
- `clearMarkedNumbersForGame(gameId)` - Reset all marks

#### Local Persistence (`data/local/`)

**Database**: SQLite database using SQLDelight
- Tables:
  - `games` - Game records
  - `bingo_cards` - Card data (serialized grid)
  - `marked_numbers` - Called numbers per game

**DatabaseDriverFactory**: Platform-specific database initialization
- Android: Uses Android driver
- iOS: Uses native SQLite driver

## Data Flow

### Creating a New Game

1. User enters game name in UI
2. `CreateGameUseCase` creates `Game` with null `targetFigure`
3. Game saved to database
4. Navigate to Card Setup

### Setting Up Card

1. User enters 5 unique numbers for each column
   - B: 1-15
   - I: 16-30
   - N: 31-45
   - G: 46-60
   - O: 61-75
   - Center is always FREE
2. `BingoCard.createCardFromGrid()` validates and creates card
3. Card saved to database via `BingoCardRepository`
4. Navigate to Figure Selection

### Selecting Win Figure

1. User taps a column to select win pattern (B, I, N, G, O, or Full Card)
2. `GameRepository.updateGame()` updates game with `targetFigure`
3. Navigate to Play screen

### Playing Game

1. Numbers 1-90 are displayed
2. User taps cards in bingo card or numbers in selector
3. `MarkNumberUseCase` marks the number
4. `CheckWinConditionUseCase` checks if won on each mark
5. When won, show congratulations modal
6. `CompleteGameUseCase` marks game as complete

### Managing Games

- **List**: `GetAllGamesUseCase` shows all games
- **Restart**: `RestartGameUseCase` clears all marks
- **Drop**: `DropGameUseCase` deletes game completely

## Number Range Rules

The bingo card strictly follows these ranges:

```
B Column: 1-15     (1-7 per number)
I Column: 16-30    (16-22 per number)
N Column: 31-45    (31-37 per number + FREE space)
G Column: 46-60    (46-52 per number)
O Column: 61-75    (61-67 per number)
```

## Win Conditions

### Column Win (5 cells)
- Complete entire column (B, I, N, G, or O)
- N column includes FREE space

### Full Card Win (25 cells)
- Mark all cells including FREE space

## Database Schema

### games
```sql
CREATE TABLE games (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    targetFigure TEXT,
    createdAt TEXT NOT NULL,
    isCompleted INTEGER DEFAULT 0,
    completedAt TEXT
);
```

### bingo_cards
```sql
CREATE TABLE bingo_cards (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    gameId INTEGER NOT NULL,
    grid TEXT NOT NULL, -- JSON serialized grid
    FOREIGN KEY(gameId) REFERENCES games(id)
);
```

### marked_numbers
```sql
CREATE TABLE marked_numbers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    gameId INTEGER NOT NULL,
    number INTEGER NOT NULL,
    FOREIGN KEY(gameId) REFERENCES games(id)
);
```

## UI Flow (Android)

```
Welcome
    ↓
Tool Selection
    ↓
Bingo Game List
    ├─→ Create New Game
    │       ↓
    │   Game Setup (name)
    │       ↓
    │   Card Setup (fill columns)
    │       ↓
    │   Figure Selection (choose B/I/N/G/O/Full)
    │       ↓
    │   Game Play
    │
    └─→ Select Existing Game
            ↓
        Game Play
```

## Key Features

### Step-by-Step Card Creation
- Guided flow for filling each column
- Visual progress indicator
- Range validation per column
- Uniqueness validation per column

### Visual Win Condition Display
- Target figure highlighted in figure selection
- Winning cells highlighted with different color
- Cells marked for target show distinct color
- Progress bar showing completion

### Game History
- All games saved to database
- Games can be restarted to replay
- Games can be dropped to delete

## Future Enhancements

1. **Multiple Cards per Game**: Support multiple cards per game
2. **Game Statistics**: Track win rates, average marks
3. **Custom Ranges**: Allow user-defined number ranges
4. **Sound Effects**: Add callout sounds
5. **Replay Feature**: Record number sequence for replay
6. **Cloud Sync**: Synchronize games across devices

## Testing Strategy

### Unit Tests (Shared Module)
- Use case logic validation
- Model serialization/deserialization
- Win condition calculations

### UI Tests (Platform-Specific)
- Navigation flows
- Card validation
- Number marking functionality

## Performance Considerations

1. **Database Queries**: Indexed by gameId for fast lookups
2. **Serialization**: JSON for flexibility with grid storage
3. **Memory**: Games with cards are lightweight (<1KB each)
4. **Concurrency**: Coroutines for non-blocking DB operations

---

Last Updated: April 2026
Architecture: Clean Architecture with Repository Pattern
Platforms: Android (Jetpack Compose), iOS (SwiftUI)

