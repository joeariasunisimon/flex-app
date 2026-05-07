package co.jarias.flexapp.ui.screens.bingo.card_scanner

import com.google.mlkit.vision.text.Text

sealed class ParseResult {
    data class Success(val grid: List<List<String>>) : ParseResult()
    data class InvalidRange(val number: Int, val column: String) : ParseResult()
    object Incomplete : ParseResult()
}

class BingoCardParser {
    private val columnRanges = listOf(1..15, 16..30, 31..45, 46..60, 61..75)
    private val columnLabels = listOf("B", "I", "N", "G", "O")

    fun parse(text: Text): ParseResult {
        val candidates = mutableListOf<DetectedNumber>()

        for (block in text.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    val value = element.text.filter { it.isDigit() }
                    val num = value.toIntOrNull()
                    if (num != null) {
                        candidates.add(
                            DetectedNumber(
                                value = num,
                                x = element.boundingBox?.centerX() ?: 0,
                                y = element.boundingBox?.centerY() ?: 0,
                                height = element.boundingBox?.height() ?: 0
                            )
                        )
                    }
                }
            }
        }

        if (candidates.size < 20) return ParseResult.Incomplete

        // 1. Filter out noise based on size (Bingo numbers are usually similar in size)
        val medianHeight = candidates.map { it.height }.sorted().let {
            if (it.isEmpty()) 0 else it[it.size / 2]
        }
        val validCandidates = candidates.filter { it.height >= medianHeight * 0.6 }

        if (validCandidates.size < 20) return ParseResult.Incomplete

        // 2. Group into 5 columns
        val sortedByX = validCandidates.sortedBy { it.x }
        val columns = mutableListOf<MutableList<DetectedNumber>>()
        
        if (sortedByX.isNotEmpty()) {
            var currentColumn = mutableListOf<DetectedNumber>()
            currentColumn.add(sortedByX[0])
            columns.add(currentColumn)
            
            // We expect 5 columns. Find the gaps.
            // If we have more than 5, it means some columns are split or there's horizontal noise.
            // If less than 5, we missed a column.
            
            // A more robust way to find 5 columns:
            // Identify the 4 largest gaps in X coordinates
            val gaps = mutableListOf<Pair<Int, Double>>() // Index to Gap Size
            for (i in 1 until sortedByX.size) {
                gaps.add(i to (sortedByX[i].x - sortedByX[i-1].x).toDouble())
            }
            
            // Ensure we have enough gaps to even try
            if (gaps.size < 4) return ParseResult.Incomplete

            val topGaps = gaps.sortedByDescending { it.second }.take(4).map { it.first }.sorted()
            
            var startIdx = 0
            val partitionedColumns = mutableListOf<List<DetectedNumber>>()
            for (gapIdx in topGaps) {
                partitionedColumns.add(sortedByX.subList(startIdx, gapIdx))
                startIdx = gapIdx
            }
            partitionedColumns.add(sortedByX.subList(startIdx, sortedByX.size))
            
            if (partitionedColumns.size == 5) {
                // 3. Fill the grid
                val finalGrid = List(5) { MutableList(5) { "" } }
                for (c in 0..4) {
                    val colNumbers = partitionedColumns[c].sortedBy { it.y }
                    val expectedCount = if (c == 2) 4 else 5
                    
                    // If we have headers (B I N G O) detected as numbers, they'll be at the top.
                    // If we have noise at the bottom, it'll be at the end.
                    // We try to find the contiguous block of expectedCount that fits the range.
                    
                    var bestMatch: List<DetectedNumber>? = null
                    for (i in 0..(colNumbers.size - expectedCount)) {
                        val sub = colNumbers.subList(i, i + expectedCount)
                        val invalidCount = sub.count { it.value !in columnRanges[c] }
                        if (bestMatch == null || invalidCount < bestMatch!!.count { it.value !in columnRanges[c] }) {
                            bestMatch = sub
                        }
                    }
                    
                    if (bestMatch == null) return ParseResult.Incomplete

                    var targetIdx = 0
                    for (r in 0..4) {
                        if (r == 2 && c == 2) {
                            finalGrid[r][c] = "" // FREE
                        } else {
                            val detected = bestMatch[targetIdx++]
                            // Even if it's an error, we report it so the user sees it in the grid.
                            finalGrid[r][c] = detected.value.toString()
                        }
                    }
                }
                
                // Final validation of ranges for the whole grid
                for (c in 0..4) {
                    for (r in 0..4) {
                        if (r == 2 && c == 2) continue
                        val valStr = finalGrid[r][c]
                        val num = valStr.toIntOrNull() ?: 0
                        if (num !in columnRanges[c]) {
                            return ParseResult.InvalidRange(num, columnLabels[c])
                        }
                    }
                }

                return ParseResult.Success(finalGrid)
            }
        }

        return ParseResult.Incomplete
    }
    
    data class DetectedNumber(val value: Int, val x: Int, val y: Int, val height: Int)
}

