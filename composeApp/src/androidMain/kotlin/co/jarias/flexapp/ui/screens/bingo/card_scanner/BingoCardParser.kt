package co.jarias.flexapp.ui.screens.bingo.card_scanner

import com.google.mlkit.vision.text.Text

class BingoCardParser {
    fun parse(text: Text): List<List<String>>? {
        val allNumbers = mutableListOf<DetectedNumber>()
        
        for (block in text.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    val value = element.text.filter { it.isDigit() }
                    val num = value.toIntOrNull()
                    if (num != null && num in 1..75) {
                        allNumbers.add(DetectedNumber(num, element.boundingBox?.centerX() ?: 0, element.boundingBox?.centerY() ?: 0))
                    }
                }
            }
        }
        
        if (allNumbers.size < 24) return null
        
        // Group by Y to find rows
        val rows = allNumbers.groupBy { it.y / 20 } // Rough grouping
            .values
            .filter { it.size >= 4 }
            .sortedBy { it.first().y }
            
        if (rows.size < 5) return null
        
        // This is a complex problem. Let's simplify: 
        // Just take the first 25 (or 24) numbers and try to arrange them by X then Y?
        // Better: sort all by Y, then for each row of 5, sort by X.
        
        val sortedByY = allNumbers.sortedBy { it.y }
        val finalGrid = mutableListOf<MutableList<String>>()
        
        // Very naive approach: take 5 rows of 5 numbers
        try {
            var idx = 0
            for (r in 0..4) {
                val row = mutableListOf<String>()
                val rowCount = if (r == 2) 4 else 5
                val rowNumbers = mutableListOf<DetectedNumber>()
                repeat(rowCount) {
                    if (idx < sortedByY.size) {
                        rowNumbers.add(sortedByY[idx])
                        idx++
                    }
                }
                rowNumbers.sortBy { it.x }
                
                var colIdx = 0
                for (c in 0..4) {
                    if (r == 2 && c == 2) {
                        row.add("")
                    } else {
                        row.add(rowNumbers.getOrNull(colIdx)?.value?.toString() ?: "")
                        colIdx++
                    }
                }
                finalGrid.add(row)
            }
            return finalGrid
        } catch (e: Exception) {
            return null
        }
    }
    
    data class DetectedNumber(val value: Int, val x: Int, val y: Int)
}
