package co.jarias.flexapp.data.repository

import co.jarias.flexapp.data.local.Database
import co.jarias.flexapp.domain.BingoCard
import co.jarias.flexapp.shared.database.BingoCard as DbBingoCard

class BingoCardRepositoryImpl(database: Database) : BingoCardRepository {

    private val queries = database.database.bingoDatabaseQueries

    override suspend fun getCardsByGameId(gameId: Long): List<BingoCard> {
        return queries.selectCardsByGameId(gameId).executeAsList().map { it.toDomain() }
    }

    override suspend fun getCardByGameId(gameId: Long): BingoCard? {
        return getCardsByGameId(gameId).firstOrNull()
    }

    override suspend fun insertCard(card: BingoCard) {
        val gridJson = BingoCard.serializeGrid(card.grid)
        val existing = queries.selectCardsByGameId(card.gameId).executeAsList()
        if (existing.isNotEmpty()) {
            queries.deleteCard(existing.first().id)
        }
        queries.insertCard(card.gameId, gridJson)
    }

    override suspend fun deleteCard(cardId: Long) {
        queries.deleteCard(cardId)
    }

    private fun DbBingoCard.toDomain(): BingoCard {
        val grid = BingoCard.deserializeGrid(grid_data)
        return BingoCard(
            id = id,
            gameId = game_id,
            grid = grid
        )
    }
}
