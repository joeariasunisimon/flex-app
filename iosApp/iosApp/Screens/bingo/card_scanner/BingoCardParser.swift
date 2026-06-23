import Foundation
import Vision

struct DetectedNumber {
    let value: Int
    let x: CGFloat
    let y: CGFloat
    let height: CGFloat
}

enum ParseResult: Equatable {
    case success(grid: [[String]])
    case invalidRange(number: Int, column: String)
    case incomplete
}

class BingoCardParser {
    private let columnRanges: [ClosedRange<Int>] = [1...15, 16...30, 31...45, 46...60, 61...75]
    private let columnLabels = ["B", "I", "N", "G", "O"]

    /// Parse recognized text observations from Vision into a 5x5 Bingo card grid.
    /// - Parameters:
    ///   - observations: Results from VNRecognizeTextRequest
    ///   - imageSize: The size of the image being processed (in pixels)
    /// - Returns: ParseResult with the grid or error
    func parse(from observations: [VNRecognizedTextObservation], imageSize: CGSize) -> ParseResult {
        let candidates = extractNumbers(from: observations, imageSize: imageSize)

        if candidates.count < 20 { return .incomplete }

        // 1. Filter out noise based on size (Bingo numbers are usually similar in size)
        let medianHeight = candidates.map { $0.height }.sorted().median()
        let validCandidates = candidates.filter { $0.height >= medianHeight * 0.6 }

        if validCandidates.count < 20 { return .incomplete }

        // 2. Group into 5 columns based on X-coordinate gaps
        let sortedByX = validCandidates.sorted { $0.x < $1.x }
        var gaps: [(index: Int, size: CGFloat)] = []
        for i in 1..<sortedByX.count {
            gaps.append((i, sortedByX[i].x - sortedByX[i - 1].x))
        }

        if gaps.count < 4 { return .incomplete }

        let topGaps = gaps.sorted { $0.size > $1.size }
            .prefix(4)
            .map { $0.index }
            .sorted()

        var partitionedColumns: [[DetectedNumber]] = []
        var startIdx = 0
        for gapIdx in topGaps {
            partitionedColumns.append(Array(sortedByX[startIdx..<gapIdx]))
            startIdx = gapIdx
        }
        partitionedColumns.append(Array(sortedByX[startIdx..<sortedByX.count]))

        if partitionedColumns.count != 5 { return .incomplete }

        // 3. Fill the grid
        var finalGrid: [[String]] = Array(repeating: Array(repeating: "", count: 5), count: 5)

        for c in 0..<5 {
            let colNumbers = partitionedColumns[c].sorted { $0.y < $1.y }
            let expectedCount = (c == 2) ? 4 : 5

            // Find the best contiguous block of expectedCount that fits the range
            var bestMatch: [DetectedNumber]? = nil
            var bestMatchInvalidCount = Int.max

            for i in 0...(colNumbers.count - expectedCount) {
                let sub = Array(colNumbers[i..<(i + expectedCount)])
                let invalidCount = sub.filter { !columnRanges[c].contains($0.value) }.count
                if bestMatch == nil || invalidCount < bestMatchInvalidCount {
                    bestMatch = sub
                    bestMatchInvalidCount = invalidCount
                }
            }

            guard let match = bestMatch else { return .incomplete }

            var targetIdx = 0
            for r in 0..<5 {
                if r == 2 && c == 2 {
                    finalGrid[r][c] = "" // FREE
                } else {
                    finalGrid[r][c] = String(match[targetIdx].value)
                    targetIdx += 1
                }
            }
        }

        // 4. Final validation of ranges
        for c in 0..<5 {
            for r in 0..<5 {
                if r == 2 && c == 2 { continue }
                guard let num = Int(finalGrid[r][c]) else { return .incomplete }
                if !columnRanges[c].contains(num) {
                    return .invalidRange(number: num, column: columnLabels[c])
                }
            }
        }

        return .success(grid: finalGrid)
    }

    // MARK: - Private

    private func extractNumbers(from observations: [VNRecognizedTextObservation], imageSize: CGSize) -> [DetectedNumber] {
        var candidates: [DetectedNumber] = []

        for observation in observations {
            // Get the top candidate text
            guard let topCandidate = observation.topCandidates(1).first else { continue }

            let text = topCandidate.string.trimmingCharacters(in: .whitespacesAndNewlines)
            let digitsOnly = text.filter { $0.isNumber }

            guard let number = Int(digitsOnly), number >= 1, number <= 75 else { continue }

            // Vision boundingBox is normalized [0,1] with bottom-left origin.
            // Convert to absolute coordinates with top-left origin for our spatial algorithm.
            let bbox = observation.boundingBox
            let absX = bbox.midX * imageSize.width
            let absY = (1.0 - bbox.midY) * imageSize.height // invert Y for top-left origin
            let absHeight = bbox.height * imageSize.height

            candidates.append(DetectedNumber(value: number, x: absX, y: absY, height: absHeight))
        }

        return candidates
    }
}

// MARK: - Array Extension

private extension Array where Element == CGFloat {
    func median() -> CGFloat {
        guard !isEmpty else { return 0 }
        let sorted = self.sorted()
        let mid = sorted.count / 2
        if sorted.count % 2 == 0 {
            return (sorted[mid - 1] + sorted[mid]) / 2.0
        } else {
            return sorted[mid]
        }
    }
}
