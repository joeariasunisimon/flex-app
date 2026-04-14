# Android UI Implementation - FlexApp Bingo Tracker

## Overview

The Android UI for FlexApp's Bingo Tracker uses **Jetpack Compose** with a robust navigation system based on the intent pattern. The UI is structured following MVVM (Model-View-ViewModel) architecture with clear separation of concerns.

## Navigation Architecture

The app uses **Compose Navigation** to manage screen transitions with type-safe routing. All routes are defined in the `Route` sealed class.

### Routes

```kotlin
sealed class Route(val route: String) {
    data object Welcome : Route("welcome")
    data object ToolSelection : Route("tool_selection")
    data object BingoGameSetup : Route("bingo_game_setup")
    data object BingoGameList : Route("bingo_game_list")
    data object BingoGamePlay : Route("bingo_game_play/{gameId}") {
        fun createRoute(gameId: Long) = "bingo_game_play/$gameId"
    }
}
```

### Navigation Flow

```
Welcome Screen
      ↓
Tool Selection Screen (Choose Game Type)
      ↓
Bingo Game List Screen (View/Manage Games)
      ↓ [New Game]     ↓ [Select Game]
Bingo Game Setup     Bingo Game Play
      ↓ [Create]          (Gameplay)
Bingo Game Play
```

## Screen Components

### 1. Welcome Screen
**File**: `ui/screens/welcome/WelcomeScreen.kt`

**Purpose**: Initial landing page introducing the application.

**Features**:
- App title: "FlexApp"
- Tagline: "Your Multi-Purpose Gaming Companion"
- Description about available games
- "Get Started" button to proceed

**Navigation**: Routes to `ToolSelection` screen

### 2. Tool Selection Screen
**File**: `ui/screens/tools/ToolSelectionScreen.kt`

**Purpose**: Allows users to choose which game/tool to use.

**Components**:
- TopAppBar with back navigation
- List of available games (currently Bingo)
- "Coming Soon" placeholder for future games

**Features**:
- `ToolCard` composable for game selection
- Back button to return to welcome
- Disabled state for unavailable games

**Navigation**: 
- Bingo selected → `BingoGameSetup`
- Back pressed → `Welcome`

### 3. Bingo Game Setup Screen
**File**: `ui/screens/bingo/BingoGameSetupScreen.kt`

**Purpose**: Create a new Bingo game with configuration.

**Sections**:

#### a) Game Name Input
- Text field to name the game
- Placeholder: "Enter game name"
- Validation: Non-empty required

#### b) Target Figure Selection
Three filter chips to choose win condition:
- "5 in a row" - Traditional straight line bingo
- "X" - Diagonal/cross pattern
- "Full card" - All 90 numbers marked

#### c) Card Numbers Selection
- Interactive 9x10 grid (90 numbers total)
- Visual feedback for selected numbers
- Toggle numbers on/off
- Display count of selected numbers

#### d) Game Creation
- Button with loading state
- Error message display
- All fields validated before creation

**Components**:
- `NumberGridSelector` - 90-number grid
- `NumberButton` - Individual number toggle
- `BingoGameViewModel` - State management

**State Management** (via ViewModel):
```kotlin
data class BingoGameSetupUiState(
    val gameName: String,
    val targetFigure: String,
    val cardNumbers: List<Int>,
    val isLoading: Boolean,
    val errorMessage: String?,
    val gameCreated: Boolean,
    val createdGameId: Long?
)
```

**Navigation**: 
- Game created successfully → `BingoGamePlay`
- Back pressed → `ToolSelection`

### 4. Bingo Game List Screen
**File**: `ui/screens/bingo/BingoGameListScreen.kt`

**Purpose**: Display and manage existing Bingo games.

**Components**:
- TopAppBar with navigation
- List of Bingo games
- Floating action button (FAB) for new game

**Features**:
- `BingoGameItem` composable for each game
- Swipe to delete game
- Tap on game to resume or continue

**Navigation**: 
- Game selected → `BingoGamePlay`
- FAB clicked → `BingoGameSetup`
- Back pressed → `ToolSelection`

## ViewModels

### BingoGameViewModel
**File**: `viewmodel/BingoGameViewModel.kt`

**Responsibilities**:
- Manage game setup state
- Handle user inputs
- Validate form data
- Orchestrate game creation via use cases
- Handle errors and loading states

**Key Methods**:
```kotlin
fun updateGameName(name: String)
fun updateTargetFigure(figure: String)
fun updateCardNumbers(numbers: List<Int>)
fun createGame()
fun resetState()
```

**State Flow**:
- Exposes `uiState: StateFlow<BingoGameSetupUiState>` for UI observation
- Uses `viewModelScope.launch` for coroutine management

## Directory Structure

```
composeApp/src/androidMain/kotlin/co/jarias/flexapp/
├── ui/
│   ├── navigation/
│   │   ├── Route.kt
│   │   └── NavGraph.kt
│   └── screens/
│       ├── welcome/
│       │   └── WelcomeScreen.kt
│       ├── tools/
│       │   └── ToolSelectionScreen.kt
│       └── bingo/
│           ├── BingoGameSetupScreen.kt
│           └── BingoGameListScreen.kt
├── viewmodel/
│   └── BingoGameViewModel.kt
├── App.kt
└── MainActivity.kt
```

## UI Patterns and Best Practices

### 1. Composable Structure
- Single responsibility per composable
- State hoisting for reusability
- Modifier chaining for layout composition

### 2. Navigation
- Type-safe routes using sealed classes
- Proper back stack management
- Navigation state persisted across configuration changes

### 3. State Management
- ViewModel for business logic state
- Composition Local for UI-only state
- StateFlow for reactive updates

### 4. Error Handling
- Form validation before submission
- User-friendly error messages
- Loading states during async operations

### 5. Accessibility
- Proper content descriptions for interactive elements
- Text hierarchy using typography system
- Sufficient touch target sizes

## Material Design Implementation

### Theme & Styling
- Uses Material 3 design system
- `MaterialTheme.colorScheme` for colors
- `MaterialTheme.typography` for text styles
- Rounded corners for modern appearance

### Components Used
- `TopAppBar` - Header with navigation
- `OutlinedTextField` - Text input
- `FilterChip` - Multi-selection
- `Button` - Primary actions
- `CircularProgressIndicator` - Loading state
- `Surface` - Container backgrounds
- `Row/Column` - Layout composition

## User Flow Example

### Creating a Bingo Game

1. **Welcome Screen**
   - User taps "Get Started"

2. **Tool Selection**
   - User sees "Bingo" option with description
   - User taps Bingo card

3. **Game Setup**
   - Enter game name: "Family Game Night"
   - Select target: "5 in a row"
   - Choose numbers: Select all 90 or subset
   - Tap "Create Game"

4. **Validation**
   - All fields validated
   - Game and card created in database

5. **Gameplay Screen**
   - Transitions to game play screen
   - Game ID passed as navigation argument

## Dependencies

```gradle
// Navigation
implementation(libs.androidx.navigation)

// Compose Material3
implementation(libs.compose.material3)

// ViewModel
implementation(libs.androidx.lifecycle.viewmodelCompose)
implementation(libs.androidx.lifecycle.runtimeCompose)

// Shared Module (Domain & Data)
implementation(projects.shared)
```

## Future Enhancements

1. **Game Playscreen**
   - Display current game state
   - Mark numbers as called
   - Visual feedback for marked numbers
   - Check win condition

2. **Game List Screen**
   - Display all active games
   - Resume/continue functionality
   - Delete game option

3. **Settings**
   - Customize appearance
   - Sound/vibration preferences
   - Difficulty levels

4. **Animations**
   - Screen transitions with fade/slide
   - Number selection animations
   - Win condition celebration animation

5. **Offline Support**
   - Auto-save game state
   - Sync when online
   - Cloud backup

## Testing Considerations

### Unit Tests
- ViewModel state changes
- Input validation logic
- Navigation decisions

### UI Tests
- Composable rendering
- User interactions (clicks, input)
- Navigation flows

### Integration Tests
- Database interactions
- Full app flow from welcome to gameplay

## Performance Optimization

1. **Recomposition Management**
   - Stable parameter types
   - Proper state scoping
   - LazyColumn for large lists (future)

2. **Memory**
   - ViewModel lifecycle management
   - Proper disposal of resources

3. **Rendering**
   - Efficient recomposition
   - Preview support for rapid iteration

---

**Last Updated**: April 12, 2026
**Architecture**: MVVM + Compose Navigation
**Design System**: Material Design 3

