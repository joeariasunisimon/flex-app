package co.jarias.flexapp.data.repository

import co.jarias.flexapp.domain.BingoCard

interface BingoCardRepository {
    suspend fun getCardsByGameId(gameId: Long): List<BingoCard>
    suspend fun getCardByGameId(gameId: Long): BingoCard?
    suspend fun insertCard(card: BingoCard)
    suspend fun deleteCard(cardId: Long)
}
