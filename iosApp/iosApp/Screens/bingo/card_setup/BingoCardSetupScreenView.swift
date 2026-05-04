import SwiftUI
import shared

struct BingoCardSetupScreenView: View {
    @StateObject private var viewModel: BingoCardSetupScreenViewModel
    private let onNavigate: (NavigationEvent) -> Void
    
    init(coordinator: AppNavCoordinator?, gameId: Int64) {
        let helper = KoinHelper()
        _viewModel = StateObject(wrappedValue: BingoCardSetupScreenViewModel(
            getBingoCardUseCase: helper.getBingoCardUseCase,
            saveBingoCardUseCase: helper.saveBingoCardUseCase,
            getCardSetupStateUseCase: helper.getCardSetupStateUseCase,
            saveCardSetupStateUseCase: helper.saveCardSetupStateUseCase,
            clearCardSetupStateUseCase: helper.clearCardSetupStateUseCase,
            gameId: gameId
        ))
        self.onNavigate = { event in
            coordinator?.handleNavigationEvent(event)
        }
    }
    
    var body: some View {
        ZStack {
            Color.backgroundWarm.ignoresSafeArea()
            
            VStack(spacing: 0) {
                if viewModel.state.isLoading {
                    Spacer()
                    ProgressView()
                        .tint(.primaryTeal)
                    Spacer()
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 16) {
                            // Progress Section
                            VStack(alignment: .leading, spacing: 8) {
                                Text("Step \(viewModel.state.currentColumn + 1) of 5: Fill the \(viewModel.state.currentColumnLabel) column")
                                    .font(.headline)
                                    .fontWeight(.bold)
                                
                                ProgressView(value: Double(viewModel.state.currentColumn + 1), total: 5)
                                    .tint(.primaryTeal)
                            }
                            .padding(.top, 8)
                            
                            let isNColumn = viewModel.state.currentColumn == 2
                            Text(isNColumn ? "Enter 4 unique numbers between 31 and 45. The center is a FREE space." : "Enter 5 unique numbers between \(viewModel.state.currentColumnRange.lowerBound) and \(viewModel.state.currentColumnRange.upperBound).")
                                .font(.subheadline)
                                .foregroundColor(.gray)
                            
                            // Card Setup Area
                            VStack(spacing: 16) {
                                HStack {
                                    Text(viewModel.state.currentColumnLabel)
                                        .font(.system(size: 32, weight: .bold))
                                        .foregroundColor(.primaryTeal)
                                    
                                    Spacer()
                                    
                                    Button(action: { viewModel.onEvent(.onRandomFill) }) {
                                        Text("Auto-fill")
                                            .font(.subheadline)
                                            .fontWeight(.bold)
                                            .foregroundColor(.accentAmber)
                                    }
                                }
                                .padding(.horizontal, 8)
                                
                                VStack(spacing: 8) {
                                    ForEach(0..<5, id: \.self) { row in
                                        if row == 2 && viewModel.state.currentColumn == 2 {
                                            Text("FREE")
                                                .frame(maxWidth: .infinity)
                                                .frame(height: 56)
                                                .background(Color.gray.opacity(0.1))
                                                .cornerRadius(8)
                                                .overlay(
                                                    RoundedRectangle(cornerRadius: 8)
                                                        .stroke(Color.gray.opacity(0.2), lineWidth: 1)
                                                )
                                        } else {
                                            TextField("Number \(row + 1)", text: Binding(
                                                get: { viewModel.state.cardNumbers[row][viewModel.state.currentColumn] },
                                                set: { viewModel.onEvent(.onCardNumberChanged(row: row, col: viewModel.state.currentColumn, value: $0)) }
                                            ))
                                            .keyboardType(.numberPad)
                                            .multilineTextAlignment(.center)
                                            .padding()
                                            .background(Color.white)
                                            .cornerRadius(8)
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 8)
                                                    .stroke(Color.primaryTeal.opacity(0.3), lineWidth: 1)
                                            )
                                        }
                                    }
                                }
                            }
                            .padding(16)
                            .background(Color.white)
                            .cornerRadius(16)
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(Color.primaryTeal, lineWidth: 2)
                            )
                            
                            if let error = viewModel.state.errorMessage {
                                Text(error)
                                    .font(.caption)
                                    .foregroundColor(.red)
                                    .padding()
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .background(Color.red.opacity(0.1))
                                    .cornerRadius(8)
                            }
                        }
                        .padding(24)
                    }
                    
                    // Bottom Buttons
                    HStack(spacing: 16) {
                        Button(action: { viewModel.onEvent(.onPreviousColumn) }) {
                            Text("Previous")
                                .fontWeight(.bold)
                                .frame(maxWidth: .infinity)
                                .frame(height: 56)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 28)
                                        .stroke(Color.primaryTeal, lineWidth: 1)
                                )
                                .foregroundColor(.primaryTeal)
                        }
                        .disabled(viewModel.state.currentColumn == 0)
                        .opacity(viewModel.state.currentColumn == 0 ? 0.5 : 1)
                        
                        Button(action: { viewModel.onEvent(.onNextColumn) }) {
                            ZStack {
                                if viewModel.state.isSaving {
                                    ProgressView()
                                        .tint(.white)
                                } else {
                                    Text(viewModel.state.currentColumn == 4 ? "Save Card" : "Next")
                                        .fontWeight(.bold)
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(Color.primaryTeal)
                            .foregroundColor(.white)
                            .cornerRadius(28)
                        }
                        .disabled(viewModel.state.isSaving)
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 32)
                }
            }
        }
        .navigationTitle("Set Up Your Bingo Card")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { onNavigate(.onNavigateUp) }) {
                    Image(systemName: "arrow.left")
                        .foregroundColor(.black)
                        .fontWeight(.bold)
                }
                .buttonStyle(.plain)
            }
            
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { onNavigate(.navigateToBingoCardScanner(gameId: viewModel.state.gameId)) }) {
                    Image(systemName: "camera")
                        .foregroundColor(.black)
                        .fontWeight(.bold)
                }
                .buttonStyle(.plain)
            }
        }
        .toolbarBackground(Color.backgroundWarm, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .onChange(of: viewModel.state.cardSaved) { _, saved in
            if saved {
                onNavigate(.navigateToBingoFigureSelection(gameId: viewModel.state.gameId))
            }
        }
    }
}

#Preview {
    NavigationStack {
        BingoCardSetupScreenView(coordinator: nil, gameId: 1)
    }
}
