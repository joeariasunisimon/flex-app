# Bingo Tracker - Implementation Roadmap

## Completed Features ✅

### Phase 1: Core Game Logic
- [x] Game creation with name
- [x] Bingo card setup (5x5 grid with numbers 1-75)
- [x] Card number validation (per-column ranges)
- [x] Win condition selection (B/I/N/G/O/Full Card)
- [x] Number marking (1-90)
- [x] Win detection for all conditions
- [x] Game state persistence (SQLDelight)

### Phase 2: UI/UX Enhancement  
- [x] Step-by-step card setup screen
- [x] Progress tracking during card creation
- [x] Enhanced figure selection with visual feedback
- [x] Game play screen with progress bar
- [x] Game list with restart/delete options
- [x] Confirmation dialogs for destructive actions
- [x] Better visual hierarchy and typography

### Phase 3: Game Management
- [x] Game listing
- [x] Restart games (clear marks, keep card)
- [x] Delete games (cascade delete all data)
- [x] Completion status tracking
- [x] Game history

---

## Next Phase: Additional Features 🚀

### Phase 4: Enhanced Gameplay (Medium Priority)

#### 4.1 Audio & Visual Feedback
```kotlin
// Add to BingoGamePlayViewModel
fun markNumberWithFeedback(number: Int) {
    // Play sound effect
    playMarkSound()
    // Vibrate device
    vibrate(50.ms)
    // Animate cell
    animateCellMarked(row, col)
    // Mark in game
    markNumber(number)
}
```

**Tasks:**
- [ ] Add sound effect library (e.g., SoundPool)
- [ ] Vibration feedback on mark
- [ ] Cell marking animation
- [ ] Win celebration animation/sound
- [ ] Audio toggle in settings

#### 4.2 Game Statistics
```kotlin
data class GameStatistics(
    val totalGamesPlayed: Int,
    val gamesWon: Int,
    val winRate: Double,
    val averageMarksNeeded: Int,
    val fastestWin: Int,
    val slowestWin: Int
)

class GetGameStatisticsUseCase(private val gameRepository: GameRepository)
```

**Tasks:**
- [ ] Add statistics to database schema
- [ ] Create statistics calculation use cases
- [ ] Update game completion to record stats
- [ ] Create statistics UI screen
- [ ] Display in game list or main menu

#### 4.3 Game Replay/Undo
```kotlin
data class MarkedNumberWithTimestamp(
    val number: Int,
    val markedAt: Long,
    val sequenceNumber: Int
)

class UndoLastMarkUseCase(
    private val markedNumberRepository: MarkedNumberRepository,
    private val gameRepository: GameRepository
)

class GetGameHistoryUseCase(private val markedNumberRepository: MarkedNumberRepository)
```

**Tasks:**
- [ ] Record mark timestamps
- [ ] Implement undo functionality
- [ ] Show mark history
- [ ] Replay game with animation
- [ ] Share game sequence

---

### Phase 5: Multi-Card Support (High Priority)

#### 5.1 Multiple Cards Per Game
```kotlin
data class Game(
    val id: Long? = null,
    val name: String,
    val targetFigure: WinCondition?,
    val createdAt: String,
    val cardCount: Int = 1,  // NEW
    val isCompleted: Boolean = false,
    val completedAt: String? = null,
    val winnerCardId: Long? = null  // NEW
)

class GenerateMultipleCardsUseCase(private val bingoCardRepository: BingoCardRepository)
class CheckMultipleCardsWinUseCase(...)
```

**Tasks:**
- [ ] Modify Game model to support multiple cards
- [ ] Update card generation logic
- [ ] Show all cards in play screen
- [ ] Mark numbers on one or all cards
- [ ] Detect win on any card
- [ ] Track which card won

#### 5.2 Card Display Options
```kotlin
sealed class CardDisplayMode {
    data object SingleCard : CardDisplayMode()
    data object AllCards : CardDisplayMode()
    data object CompactGrid : CardDisplayMode()
}
```

**Tasks:**
- [ ] Implement card selector UI
- [ ] Create compact card grid view
- [ ] Support horizontal scrolling between cards
- [ ] Show marks across all cards

---

### Phase 6: Custom Ranges (Medium Priority)

#### 6.1 User-Defined Number Ranges
```kotlin
data class BingoRules(
    val minNumber: Int = 1,
    val maxNumber: Int = 90,
    val columnCount: Int = 5,
    val rowCount: Int = 5,
    val columnRanges: List<IntRange> = (1..5).map { col ->
        (minNumber + (col-1)*18)..(minNumber + col*18 - 1)
    }
)

class ValidateNumberRangeUseCase(private val bingoRules: BingoRules)
```

**Tasks:**
- [ ] Create custom rules configuration screen
- [ ] Validate custom ranges
- [ ] Store rules in database
- [ ] Apply rules to card generation
- [ ] Save/load rule presets

---

### Phase 7: Social Features (Lower Priority)

#### 7.1 Game Sharing
```kotlin
class ExportGameUseCase(private val bingoCardRepository: BingoCardRepository)
class ImportGameUseCase(private val bingoCardRepository: BingoCardRepository)
class ShareGameLinkUseCase()

data class GameExport(
    val gameName: String,
    val targetFigure: WinCondition,
    val cardNumbers: List<List<Int?>>,
    val exportFormat: String = "json"
)
```

**Tasks:**
- [ ] Export game as JSON/CSV
- [ ] Create share link generation
- [ ] Implement QR code for sharing
- [ ] Import games from file
- [ ] Cloud sync support

#### 7.2 Multiplayer (Future)
- Real-time game synchronization
- Multiple players per game
- Live score tracking
- Chat during gameplay

---

## Architecture Improvements

### 1. Dependency Injection
```kotlin
// Current: Manual creation in composables
val database = remember { Database(DatabaseDriverFactory(context)) }

// Ideal: Hilt/Koin
@HiltViewModel
class BingoGameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val cardRepository: BingoCardRepository
) : ViewModel()
```

**Tasks:**
- [ ] Integrate Hilt for Android
- [ ] Setup Koin for KMP
- [ ] Replace manual DI in composables
- [ ] Create module definitions

### 2. Error Handling
```kotlin
sealed class GameError : Exception() {
    data class GameNotFound(val gameId: Long) : GameError()
    data class CardNotFound(val gameId: Long) : GameError()
    data class InvalidNumberRange(val number: Int, val column: Int) : GameError()
    data class DatabaseError(val message: String) : GameError()
}
```

**Tasks:**
- [ ] Define error hierarchy
- [ ] Implement error handling in use cases
- [ ] Add error UI states
- [ ] Improve error messages to users

### 3. Testing Framework
```kotlin
// Unit tests for use cases
@Test
fun markNumber_whenValidNumber_shouldSucceed() {
    val useCase = MarkNumberUseCase(markedRepository)
    val result = runBlocking { useCase(gameId = 1, number = 5) }
    assertTrue(result)
}

// UI tests for navigation
@Test
fun navigationFlow_createGame_navigatesToCardSetup() {
    // Test navigation from game setup to card setup
}
```

**Tasks:**
- [ ] Setup JUnit for shared module tests
- [ ] Setup Espresso for UI tests
- [ ] Write tests for all use cases
- [ ] Write navigation tests
- [ ] Achieve 80%+ code coverage

---

## Database Enhancements

### Current Schema
```sql
games
bingo_cards
marked_numbers
```

### Proposed Additions
```sql
game_statistics (winRate, avgMarks, etc.)
marked_numbers_with_timestamp (for history/replay)
bingo_rules (custom ranges)
game_exports (for sharing)
```

**Tasks:**
- [ ] Design new tables
- [ ] Create migrations
- [ ] Implement repositories
- [ ] Update use cases

---

## iOS Implementation (SwiftUI)

Once Android is stable, implement iOS version:

**Estimated Tasks:**
- [ ] Setup iOS app structure
- [ ] Create SwiftUI screens mirroring Android
- [ ] Integrate KMP shared module
- [ ] Test database access
- [ ] Implement platform-specific features
- [ ] iOS-specific UI refinements

---

## Performance Optimization

### Current Status
- ✅ SQLite queries optimized with indexes
- ✅ Coroutine-based async operations
- ✅ Proper memory management with remember {}

### Future Optimizations
- [ ] Implement database connection pooling
- [ ] Add query result caching
- [ ] Optimize image loading (if adding images)
- [ ] Profile and optimize recompositions
- [ ] Implement Paging3 for large lists

---

## Documentation Tasks

- [x] Architecture documentation (BINGO_ARCHITECTURE.md)
- [x] Enhancement summary (BINGO_ENHANCEMENTS.md)
- [x] Quick reference guide (BINGO_QUICK_REFERENCE.md)
- [ ] API documentation (KDoc comments)
- [ ] Video tutorials
- [ ] Setup guide for new developers
- [ ] Contributing guidelines

---

## Release Roadmap

### v1.0 (Current - April 2026)
- Core game creation and play
- Card setup with validation
- Single card per game
- Game list with restart/delete
- SQLite persistence
- Android only

### v1.1 (Next - May 2026)
- Audio/visual feedback
- Game statistics
- Undo functionality
- Better error handling
- UI refinements

### v1.2 (Summer 2026)
- Multiple cards per game
- Custom number ranges
- Game sharing/export
- iOS implementation

### v2.0 (Fall 2026)
- Multiplayer support
- Cloud synchronization
- Advanced statistics
- Offline play

---

## Known Limitations & TODOs

- [ ] Only one card per game (v1.1: multiple cards)
- [ ] No internet connectivity needed (v2.0: cloud sync)
- [ ] Limited customization (v1.2: custom rules)
- [ ] No sound effects (v1.1: audio feedback)
- [ ] No game history/replay (v1.1: implement)
- [ ] No statistics (v1.1: add tracking)
- [ ] iOS not started (v1.2: target)

---

## Dependencies Added

### Shared Module
- Kotlin Multiplatform
- SQLDelight
- Kotlinx.datetime
- Kotlinx.serialization

### Android
- Jetpack Compose
- Jetpack Compose Navigation
- Android Lifecycle

### Future
- Hilt (dependency injection)
- Retrofit (for cloud sync)
- Room (optional, if replacing SQLDelight)

---

## Testing Coverage Goals

| Module | Current | Target |
|--------|---------|--------|
| Domain (Use Cases) | 0% | 90% |
| Data (Repository) | 0% | 80% |
| UI (Navigation) | 0% | 70% |
| Overall | 0% | 80% |

---

**Last Updated**: April 13, 2026
**Next Review**: May 13, 2026
**Status**: Active Development

