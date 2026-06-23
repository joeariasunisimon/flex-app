import Foundation

enum BingoCardScannerScreenEvents {
    case onNumbersDetected(grid: [[String]])
    case onScanFailed(message: String)
    case onStartScan
    case onConfirmSave
    case onRetry
    case onErrorModalDismissed
}
