import SwiftUI
import shared

struct ToolSelectionScreenView: View {
    @StateObject private var viewModel = ToolSelectionScreenViewModel()
    private let onNavigate: (NavigationEvent) -> Void
    
    init(coordinator: AppNavCoordinator?) {
        self.onNavigate = { event in
            coordinator?.handleNavigationEvent(event)
        }
    }
    
    var body: some View {
        ZStack {
            Color.backgroundWarm
                .ignoresSafeArea()
            
            VStack(alignment: .leading, spacing: 24) {
                Text("Choose a game to play")
                    .font(.headline)
                    .fontWeight(.bold)
                    .padding(.horizontal)
                    .padding(.top, 16)
                
                VStack(spacing: 16) {
                    ToolCard(
                        title: "Bingo",
                        description: "Play the classic bingo game with customizable cards and target figures",
                        iconName: "die.face.5",
                        enabled: true
                    ) {
                        viewModel.onEvent(.onToolSelected(tool: "Bingo"))
                        onNavigate(.navigateToBingoGameList)
                    }
                    
                    ToolCard(
                        title: "Coming Soon...",
                        description: "More exciting games will be available soon!",
                        iconName: "rocket.fill",
                        enabled: false
                    ) { }
                }
                .padding(.horizontal)
                
                Spacer()
            }
        }
        .navigationTitle("Select a Game")
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct ToolCard: View {
    let title: String
    let description: String
    let iconName: String
    let enabled: Bool
    let onClick: () -> Void
    
    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 16) {
                Image(systemName: iconName)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 48, height: 48)
                    .padding(8)
                    .background(Color.primaryTeal.opacity(0.1))
                    .cornerRadius(12)
                    .foregroundColor(.primaryTeal)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(.black)
                    
                    Text(description)
                        .font(.caption)
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.leading)
                }
                
                Spacer()
            }
            .padding()
            .background(enabled ? Color.surfaceElevated : Color.surfaceElevated.opacity(0.5))
            .cornerRadius(16)
        }
        .disabled(!enabled)
    }
}

#Preview {
    ToolSelectionScreenView(coordinator: nil)
}
