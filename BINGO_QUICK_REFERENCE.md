# Bingo Tracker - Quick Developer Reference

## Project Structure

```
FlexApp/
├── shared/                          # KMP shared module
│   ├── src/commonMain/
│   │   └── kotlin/co/jarias/flexapp/
│   │       ├── domain/
│   │       │   ├── model.kt         # Data models: Game, BingoCard, WinCondition
│   │       │   └── usecase/
│   │       │       ├── CreateGameUseCase.kt
│   │       │       ├── BingoGameUseCases.kt
│   │       │       └── GameManagementUseCases.kt
│   │       └── data/
│   │           ├── local/           # SQLite database
│   │           └── repository/      # Repository implementations
│   ├── BINGO_ARCHITECTURE.md        # Full architecture documentation
│   └── build.gradle.kts
│
└── composeApp/                      # Android app
    ├── src/androidMain/
    │   ├── kotlin/co/jarias/flexapp/
    │   │   ├── ui/
    │   │   │   ├── navigation/
    │   │   │   │   ├── Route.kt     # Navigation routes
    │   │   │   │   └── NavGraph.kt
    │   │   │   └── screens/
    │   │   │       ├── welcome/
    │   │   │       ├── tools/
    │   │   │       └── bingo/
    │   │   │           ├── BingoGameSetupScreen.kt      # Create game
    │   │   │           ├── BingoCardSetupScreen.kt      # Fill columns
    │   │   │           ├── BingoFigureSelectionScreen.kt # Select figure
    │   │   │           ├── BingoGamePlayScreen.kt       # Play game
    │   │   │           └── BingoGameListScreen.kt       # List & manage
    │   │   └── viewmodel/           # ViewModels for UI state
    │   └── AndroidManifest.xml
    └── build.gradle.kts
```

## Key Concepts

### WinCondition Enum
```kotlin
enum class WinCondition(val displayName: String, val requiredCells: Set<Pair<Int, Int>>) {
    B("B", 5 cells in column 0),
    I("I", 5 cells in column 1),
    N("N", 5 cells in column 2 including FREE),
    G("G", 5 cells in column 3),
    O("O", 5 cells in column 4),
    FULL_CARD("Full Card", all 25 cells)
}
```

### Number Ranges (Strict by Column)
```
B → 1-15      (column 0)
I → 16-30     (column 1)
N → 31-45     (column 2)
G → 46-60     (column 3)
O → 61-75     (column 4)
```

### Navigation Flow
```
Welcome → Tool Selection → Bingo Games List
                                ├→ Create New Game
                                │  └→ Card Setup
                                │     └→ Figure Selection
                                │        └→ Game Play
                                │
                                └→ Select Existing Game
                                   └→ Game Play
```

## Common Tasks

### Create a New Game
```kotlin
val createGameUseCase = CreateGameUseCase(gameRepository)
val game = createGameUseCase("My Game", targetFigure = null)
// Returns Game with id set by database
```

### Setup a Card
```kotlin
val cardNumbers = listOf(
    listOf(5, 22, 43, 57, 71),
    listOf(12, 29, 34, 60, 68),
    listOf(3, 18, null, 52, 75),  // null for FREE space
    listOf(10, 25, 40, 56, 64),
    listOf(7, 16, 38, 63, 72)
)
val grid = BingoCard.createCardFromGrid(cardNumbers)
val card = BingoCard(gameId = gameId, grid = grid)
bingoCardRepository.insertCard(card)
```

### Mark a Number
```kotlin
val markUseCase = MarkNumberUseCase(markedNumberRepository, bingoCardRepository)
val success = markUseCase(gameId, 5)  // Returns false if already marked
```

### Check Win Condition
```kotlin
val checkWinUseCase = CheckWinConditionUseCase(bingoCardRepository, markedNumberRepository)
val isWon = checkWinUseCase(gameId, WinCondition.B)
```

### Restart a Game
```kotlin
val restartUseCase = RestartGameUseCase(gameRepository, markedNumberRepository)
restartUseCase(gameId)  // Clears marks, resets completion
```

### Delete a Game
```kotlin
val dropUseCase = DropGameUseCase(gameRepository, bingoCardRepository, markedNumberRepository)
dropUseCase(gameId)  // Removes game, card, and all marks
```

## UI State Management

### BingoGameViewModel
```kotlin
data class BingoGameUiState(
    val gameName: String = "",
    val gameCreated: Boolean = false,
    val createdGameId: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

### BingoGamePlayViewModel
```kotlin
data class BingoGamePlayUiState(
    val gameState: GameState? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showWinDialog: Boolean = false
)
```

## Important Files to Know

| File | Purpose | Key Classes |
|------|---------|-------------|
| `model.kt` | Data models | Game, BingoCard, WinCondition |
| `CreateGameUseCase.kt` | Game creation | CreateGameUseCase |
| `BingoGameUseCases.kt` | Game logic | MarkNumber, CheckWin, GetGameState |
| `GameManagementUseCases.kt` | Game mgmt | Restart, Drop, UpdateName |
| `Route.kt` | Navigation routes | Route enum |
| `NavGraph.kt` | Navigation setup | AppNavigation composable |

## Testing Checklist

- [ ] Can create game with name
- [ ] Card setup validates column ranges
- [ ] Card setup requires all 5 columns filled
- [ ] Figure selection shows correct cell count
- [ ] Can mark numbers during gameplay
- [ ] Win detection works for all conditions
- [ ] Progress bar updates in real-time
- [ ] Restart clears marks
- [ ] Delete removes all data
- [ ] Navigation works smoothly

## Debugging Tips

### Check Database State
```kotlin
val games = gameRepository.getAllGames()  // See all games
val cards = bingoCardRepository.getCardsByGameId(gameId)
val marks = markedNumberRepository.getMarkedNumbersByGameId(gameId)
```

### Verify Win Condition
```kotlin
val gameState = getGameStateUseCase(gameId)
println("Card: ${gameState.card.grid}")
println("Marks: ${gameState.markedNumbers}")
println("Target cells: ${gameState.game.targetFigure?.requiredCells}")
```

### UI State Debugging
Add logging in ViewModels:
```kotlin
fun markNumber(number: Int) {
    viewModelScope.launch {
        try {
            val success = markNumberUseCase(gameId, number)
            println("Mark $number: $success")
            loadGame(gameId)  // Refresh state
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }
}
```

## Performance Tips

1. **Use `withContext(Dispatchers.IO)`** for database operations
2. **Remember {} blocks** for expensive computations (database objects)
3. **LazyColumn** for large lists (future: many games)
4. **StateFlow** for reactive UI updates
5. **Coroutines** for non-blocking operations

## Common Errors & Solutions

### "Game not found"
- Check if game was created successfully
- Verify gameId is correct
- Look at database logs

### "Mark already exists"
- MarkNumberUseCase returns false if already marked
- Check markedNumbers set before marking

### "Database locked"
- Ensure you're using proper coroutine dispatchers
- Use IO dispatcher for database operations

### "Navigation doesn't work"
- Verify Route.kt has the route defined
- Check NavGraph.kt has composable entry
- Ensure arguments are passed correctly

## Resources

- **Architecture**: See `BINGO_ARCHITECTURE.md`
- **Enhancements**: See `BINGO_ENHANCEMENTS.md`
- **KMP Docs**: https://kotlinlang.org/docs/multiplatform.html
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **SQLDelight**: https://cashapp.github.io/sqldelight/

---

**Last Updated**: April 13, 2026
**Version**: 1.0
**Status**: Stable

