//
//  AppNavCoordinator.swift
//  FlexApp
//
//  Created by Joe Arias on 16/04/26.
//

import SwiftUI
import Combine

enum ScreenPath: String, Identifiable, CaseIterable {
    var id: String { rawValue }
    
    // Main screens
    case welcome
    case toolSelection
    case bingoGameList
    case bingoGameSetup
    
    // Bingo flow
    case bingoCardSetup
    case bingoFigureSelection
    case bingoGamePlay
}

class AppNavCoordinator: ObservableObject {
    @Published var path = [ScreenPath]()
    
    func push(_ screen: ScreenPath) {
        path.append(screen)
    }
    
    func pop() {
        path.removeLast()
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
        case .bingoCardSetup:
            BingoCardSetupScreenView(coordinator: self, gameId: 0)
        case .bingoFigureSelection:
            BingoFigureSelectionScreenView(coordinator: self, gameId: 0)
        case .bingoGamePlay:
            BingoGamePlayScreenView(coordinator: self, gameId: 0)
        }
    }
}

// MARK: - Navigation Helpers
extension AppNavCoordinator {
    func goToWelcome() {
        path = []
    }
    
    func goToToolSelection() {
        path = [.toolSelection]
    }
    
    func goToBingoGameList() {
        push(.bingoGameList)
    }
    
    func goToBingoGameSetup() {
        push(.bingoGameSetup)
    }
    
    func goToBingoCardSetup(gameId: Int) {
        push(.bingoCardSetup)
    }
    
    func goToBingoFigureSelection(gameId: Int) {
        push(.bingoFigureSelection)
    }
    
    func goToBingoGamePlay(gameId: Int) {
        push(.bingoGamePlay)
    }
}