import Foundation
import shared

class BingoCardSetupScreenViewModel: ObservableObject {
    @Published var state: BingoCardSetupScreenState
    
    private let getBingoCardUseCase: GetBingoCardUseCase
    private let saveBingoCardUseCase: SaveBingoCardUseCase
    private let getCardSetupStateUseCase: GetCardSetupStateUseCase
    private let saveCardSetupStateUseCase: SaveCardSetupStateUseCase
    private let clearCardSetupStateUseCase: ClearCardSetupStateUseCase
    private let generateRandomNumbersUseCase = GenerateRandomNumbersUseCase()
    
    init(
        getBingoCardUseCase: GetBingoCardUseCase,
        saveBingoCardUseCase: SaveBingoCardUseCase,
        getCardSetupStateUseCase: GetCardSetupStateUseCase,
        saveCardSetupStateUseCase: SaveCardSetupStateUseCase,
        clearCardSetupStateUseCase: ClearCardSetupStateUseCase,
        gameId: Int64
    ) {
        self.getBingoCardUseCase = getBingoCardUseCase
        self.saveBingoCardUseCase = saveBingoCardUseCase
        self.getCardSetupStateUseCase = getCardSetupStateUseCase
        self.saveCardSetupStateUseCase = saveCardSetupStateUseCase
        self.clearCardSetupStateUseCase = clearCardSetupStateUseCase
        self.state = BingoCardSetupScreenState(gameId: gameId)
        loadGame(gameId: gameId)
    }
    
    func onEvent(_ event: BingoCardSetupScreenEvents) {
        switch event {
        case .onCardNumberChanged(let row, let col, let value):
            updateCardNumber(row: row, col: col, value: value)
        case .onNextColumn:
            nextColumn()
        case .onPreviousColumn:
            previousColumn()
        case .onSaveCard:
            saveCard()
        case .onRandomFill:
            randomFillCurrentColumn()
        }
    }
    
    private func loadGame(gameId: Int64) {
        state.isLoading = true
        getBingoCardUseCase.invoke(gameId: gameId) { [weak self] existingCard, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                if let card = existingCard {
                    let cardNumbers = card.grid.map { row in
                        row.map { cell in
                            cell.number?.stringValue ?? ""
                        }
                    }
                    self.state.cardNumbers = cardNumbers
                    self.state.isLoading = false
                    self.findFirstIncompleteColumn()
                } else {
                    self.loadCardSetupStateFromPreferences()
                    self.state.isLoading = false
                }
            }
        }
    }
    
    private func loadCardSetupStateFromPreferences() {
        getCardSetupStateUseCase.invoke(gameId: state.gameId) { [weak self] savedState, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                if let it = savedState {
                    self.state.currentColumn = Int(it.currentColumn)
                    self.state.cardNumbers = it.cardNumbers
                }
            }
        }
    }
    
    private func saveCardSetupStateToPreferences() {
        let cardSetupState = CardSetupState(currentColumn: Int32(state.currentColumn), cardNumbers: state.cardNumbers)
        saveCardSetupStateUseCase.invoke(gameId: state.gameId, state: cardSetupState) { error in }
    }
    
    private func clearCardSetupStateInPreferences() {
        clearCardSetupStateUseCase.invoke(gameId: state.gameId) { error in }
    }
    
    private func findFirstIncompleteColumn() {
        for col in 0..<5 {
            if !validateColumn(col: col) {
                state.currentColumn = col
                return
            }
        }
    }
    
    private func validateColumn(col: Int) -> Bool {
        let cardNumbers = state.cardNumbers
        var seen = Set<Int>()
        for row in 0..<5 {
            if row == 2 && col == 2 { continue }
            let value = cardNumbers[row][col]
            if value.trimmingCharacters(in: .whitespaces).isEmpty { return false }
            guard let num = Int(value) else { return false }
            let ranges = [1...15, 16...30, 31...45, 46...60, 61...75]
            let range = ranges[col]
            if !range.contains(num) { return false }
            if seen.contains(num) { return false }
            seen.insert(num)
        }
        return true
    }
    
    private func updateCardNumber(row: Int, col: Int, value: String) {
        state.cardNumbers[row][col] = value
        state.errorMessage = nil
        saveCardSetupStateToPreferences()
    }
    
    private func nextColumn() {
        if !validateColumn(col: state.currentColumn) {
            state.errorMessage = "Please fill all fields with valid, unique numbers in the correct range."
            return
        }
        
        saveCardSetupStateToPreferences()
        
        if state.currentColumn < 4 {
            state.currentColumn += 1
            state.errorMessage = nil
        } else {
            saveCard()
        }
    }
    
    private func previousColumn() {
        saveCardSetupStateToPreferences()
        if state.currentColumn > 0 {
            state.currentColumn -= 1
            state.errorMessage = nil
        }
    }
    
    private func randomFillCurrentColumn() {
        let currentColumn = state.currentColumn
        let randomNumbers = generateRandomNumbersUseCase.invoke(columnIndex: Int32(currentColumn))
        let numberList = Array(randomNumbers).map { ($0 as? Int) ?? 0 }
        
        for rowIdx in 0..<5 {
            if rowIdx == 2 && currentColumn == 2 { continue }
            if state.cardNumbers[rowIdx][currentColumn].isEmpty {
                let numIdx: Int
                if currentColumn == 2 {
                    numIdx = rowIdx < 2 ? rowIdx : rowIdx - 1
                } else {
                    numIdx = rowIdx
                }
                if numIdx < numberList.count {
                    state.cardNumbers[rowIdx][currentColumn] = String(numberList[numIdx])
                }
            }
        }
        
        state.errorMessage = nil
        saveCardSetupStateToPreferences()
    }
    
    private func saveCard() {
        if !validateColumn(col: state.currentColumn) {
            state.errorMessage = "Please fill all fields with valid, unique numbers in the correct range."
            return
        }
        
        for col in 0..<5 {
            if !validateColumn(col: col) {
                state.currentColumn = col
                state.errorMessage = "Please fill all fields with valid, unique numbers."
                return
            }
        }
        
        state.isSaving = true
        let grid = getBingoGrid()
        let card = BingoCard(id: nil, gameId: state.gameId, grid: grid)
        
        saveBingoCardUseCase.invoke(card: card) { [weak self] error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                self.state.isSaving = false
                if let error = error {
                    self.state.errorMessage = "Failed to save card: \(error.localizedDescription)"
                } else {
                    self.clearCardSetupStateInPreferences()
                    self.state.cardSaved = true
                }
            }
        }
    }
    
    private func getBingoGrid() -> [[BingoCell]] {
        return (0..<5).map { row in
            (0..<5).map { col in
                if row == 2 && col == 2 {
                    return BingoCell(number: nil, isMarked: false, isFree: true)
                } else {
                    let num = Int32(state.cardNumbers[row][col]) ?? 0
                    return BingoCell(number: KotlinInt(value: num), isMarked: false, isFree: false)
                }
            }
        }
    }
}
