package co.jarias.flexapp.data.repository

import co.jarias.flexapp.data.local.Database
import co.jarias.flexapp.domain.MarkedNumber
import co.jarias.flexapp.shared.database.MarkedNumber as DbMarkedNumber

class MarkedNumberRepositoryImpl(private val database: Database) : MarkedNumberRepository {

    private val queries = database.database.bingoDatabaseQueries

    override suspend fun getMarkedNumbersByGameId(gameId: Long): List<MarkedNumber> {
        return queries.selectMarkedNumbersByGameId(gameId).executeAsList().map { it.toDomain() }
    }

    override suspend fun insertMarkedNumber(markedNumber: MarkedNumber) {
        queries.insertMarkedNumber(markedNumber.gameId, markedNumber.number.toLong())
    }

    override suspend fun deleteMarkedNumber(markedNumberId: Long) {
        queries.deleteMarkedNumber(markedNumberId)
    }

    override suspend fun clearMarkedNumbersForGame(gameId: Long) {
        queries.deleteAllMarkedNumbersForGame(gameId)
    }

    private fun DbMarkedNumber.toDomain(): MarkedNumber {
        return MarkedNumber(
            id = id,
            gameId = game_id,
            number = number.toInt()
        )
    }
}
