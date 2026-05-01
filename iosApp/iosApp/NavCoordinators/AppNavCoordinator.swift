import SwiftUI
import Combine
import shared

enum ScreenPath: Hashable {
    // Main screens
    case welcome
    case toolSelection
    case bingoGameList
    case bingoGameSetup
    
    // Bingo flow
    case bingoCardSetup(gameId: Int64)
    case bingoCardScanner(gameId: Int64)
    case bingoFigureSelection(gameId: Int64)
    case bingoGamePlay(gameId: Int64)
}

class AppNavCoordinator: ObservableObject {
    @Published var path = [ScreenPath]()
    
    func handleNavigationEvent(_ event: NavigationEvent) {
        switch event {
        case .onNavigateUp:
            pop()
        case .navigateToToolSelection:
            path = [.toolSelection]
        case .navigateToBingoGameList:
            path = [.toolSelection, .bingoGameList]
        case .navigateToBingoGameSetup:
            push(.bingoGameSetup)
        case .navigateToBingoCardSetup(let gameId):
            push(.bingoCardSetup(gameId: gameId))
        case .navigateToBingoCardScanner(let gameId):
            push(.bingoCardScanner(gameId: gameId))
        case .navigateToBingoFigureSelection(let gameId):
            push(.bingoFigureSelection(gameId: gameId))
        case .navigateToBingoGamePlay(let gameId):
            push(.bingoGamePlay(gameId: gameId))
        case .navigateToTool(let toolType):
            if toolType == .bingo {
                path = [.toolSelection, .bingoGameList]
            } else {
                path = [.toolSelection]
            }
        }
    }
    
    private func push(_ screen: ScreenPath) {
        path.append(screen)
    }
    
    private func pop() {
        if !path.isEmpty {
            path.removeLast()
        }
    }
    
    func popToRoot() {
        path = []
    }
    
    @ViewBuilder
    func viewForScreen(_ screen: ScreenPath) -> some View {
        switch screen {
        case .welcome:
            WelcomeScreenView(coordinator: self)
        case .toolSelection:
            ToolSelectionScreenView(coordinator: self)
        case .bingoGameList:
            BingoGameListScreenView(coordinator: self)
        case .bingoGameSetup:
            BingoGameSetupScreenView(coordinator: self)
        case .bingoCardSetup(let gameId):
            BingoCardSetupScreenView(coordinator: self, gameId: gameId)
        case .bingoCardScanner(let gameId):
            Text("Scanner for \(gameId)") // Placeholder
        case .bingoFigureSelection(let gameId):
            BingoFigureSelectionScreenView(coordinator: self, gameId: gameId)
        case .bingoGamePlay(let gameId):
            BingoGamePlayScreenView(coordinator: self, gameId: gameId)
        }
    }
}
