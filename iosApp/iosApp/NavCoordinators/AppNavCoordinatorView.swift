//
//  AppNavCoordinatorView.swift
//  FlexApp
//
//  Created by Joe Arias on 16/04/26.
//

import SwiftUI

struct AppNavCoordinatorView: View {
    @StateObject var coordinator = AppNavCoordinator()
    
    var body: some View {
        NavigationStack(path: $coordinator.path) {
            WelcomeScreenView(coordinator: coordinator)
                .navigationDestination(for: ScreenPath.self) { screen in
                    coordinator.viewForScreen(screen)
                }
        }
    }
}

#Preview {
    AppNavCoordinatorView()
}