package co.jarias.flexapp.data.repository

import co.jarias.flexapp.domain.MarkedNumber

interface MarkedNumberRepository {
    suspend fun getMarkedNumbersByGameId(gameId: Long): List<MarkedNumber>
    suspend fun insertMarkedNumber(markedNumber: MarkedNumber)
    suspend fun deleteMarkedNumber(markedNumberId: Long)
    suspend fun clearMarkedNumbersForGame(gameId: Long)
}
