import SwiftUI

struct ToolSelectionScreenView: View {
    @StateObject private var viewModel = ToolSelectionScreenViewModel()
    private let onNavigate: (NavigationEvent) -> Void
    
    init(coordinator: AppNavCoordinator?) {
        self.onNavigate = { event in
            coordinator?.handleNavigationEvent(event)
        }
    }
    
    var body: some View {
        VStack {
            Text("Tool Selection")
                .font(.largeTitle)
            
            Spacer()
            
            ForEach(viewModel.state.availableTools, id: \.self) { tool in
                Button("Select \(tool)") {
                    viewModel.onEvent(.onToolSelected(tool: tool))
                    if tool == "Bingo" {
                        onNavigate(.navigateToBingoGameList)
                    }
                }
                .buttonStyle(.borderedProminent)
                .padding(.bottom, 10)
            }
        }
        .padding()
    }
}

#Preview {
    ToolSelectionScreenView(coordinator: nil)
}
