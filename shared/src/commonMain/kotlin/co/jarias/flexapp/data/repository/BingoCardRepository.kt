package co.jarias.flexapp.data.repository

import co.jarias.flexapp.domain.BingoCard

interface BingoCardRepository {
    suspend fun getCardById(cardId: Long): BingoCard?
    suspend fun getCardByGrid(grid: List<List<co.jarias.flexapp.domain.BingoCell>>): BingoCard?
    suspend fun insertCard(card: BingoCard): Long
    suspend fun deleteCard(cardId: Long)
    suspend fun getCardUsageCount(cardId: Long): Long
}
