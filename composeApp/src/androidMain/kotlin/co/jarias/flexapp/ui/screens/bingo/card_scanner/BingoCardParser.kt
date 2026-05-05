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
        val allNumbers = mutableListOf<DetectedNumber>()
        
        for (block in text.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    val value = element.text.filter { it.isDigit() }
                    val num = value.toIntOrNull()
                    if (num != null && num in 1..75) {
                        allNumbers.add(DetectedNumber(
                            value = num, 
                            x = element.boundingBox?.centerX() ?: 0, 
                            y = element.boundingBox?.centerY() ?: 0
                        ))
                    }
                }
            }
        }
        
        if (allNumbers.size < 24) return ParseResult.Incomplete
        
        val sortedByY = allNumbers.sortedBy { it.y }
        val finalGrid = mutableListOf<MutableList<String>>()
        
        try {
            var idx = 0
            for (r in 0..4) {
                val rowNumbers = mutableListOf<DetectedNumber>()
                val rowCount = if (r == 2) 4 else 5
                repeat(rowCount) {
                    if (idx < sortedByY.size) {
                        rowNumbers.add(sortedByY[idx])
                        idx++
                    }
                }
                
                rowNumbers.sortBy { it.x }
                
                val row = mutableListOf<String>()
                var colIdx = 0
                for (c in 0..4) {
                    if (r == 2 && c == 2) {
                        row.add("") // FREE
                    } else {
                        val detected = rowNumbers.getOrNull(colIdx)
                        if (detected != null) {
                            val range = columnRanges[c]
                            if (detected.value !in range) {
                                return ParseResult.InvalidRange(detected.value, columnLabels[c])
                            }
                            row.add(detected.value.toString())
                        } else {
                            return ParseResult.Incomplete
                        }
                        colIdx++
                    }
                }
                finalGrid.add(row)
            }
            
            val allFilledNumbers = finalGrid.flatten().filter { it.isNotEmpty() }.map { it.toInt() }
            if (allFilledNumbers.size < 24 || allFilledNumbers.distinct().size < 24) return ParseResult.Incomplete
            
            return ParseResult.Success(finalGrid)
        } catch (e: Exception) {
            return ParseResult.Incomplete
        }
    }
    
    data class DetectedNumber(val value: Int, val x: Int, val y: Int)
}
