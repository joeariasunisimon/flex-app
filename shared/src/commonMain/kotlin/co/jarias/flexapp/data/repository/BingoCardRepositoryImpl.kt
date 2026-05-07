package co.jarias.flexapp.data.repository

import co.jarias.flexapp.data.local.Database
import co.jarias.flexapp.domain.BingoCard
import co.jarias.flexapp.shared.database.BingoCard as DbBingoCard

class BingoCardRepositoryImpl(database: Database) : BingoCardRepository {

    private val queries = database.database.bingoDatabaseQueries

    override suspend fun getCardById(cardId: Long): BingoCard? {
        return queries.selectCardById(cardId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getCardByGrid(grid: List<List<co.jarias.flexapp.domain.BingoCell>>): BingoCard? {
        val gridJson = co.jarias.flexapp.domain.BingoCard.serializeGrid(grid)
        return queries.selectCardByGrid(gridJson).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun insertCard(card: BingoCard): Long {
        val gridJson = co.jarias.flexapp.domain.BingoCard.serializeGrid(card.grid)
        val existing = queries.selectCardByGrid(gridJson).executeAsOneOrNull()
        if (existing != null) {
            return existing.id
        }
        queries.insertCard(gridJson, card.createdAt)
        return queries.selectCardByGrid(gridJson).executeAsOne().id
    }

    override suspend fun deleteCard(cardId: Long) {
        queries.deleteCard(cardId)
    }

    override suspend fun getCardUsageCount(cardId: Long): Long {
        return queries.getCardUsageCount(cardId).executeAsOne()
    }

    private fun co.jarias.flexapp.shared.database.BingoCard.toDomain(): BingoCard {
        val grid = co.jarias.flexapp.domain.BingoCard.deserializeGrid(grid_data)
        return BingoCard(
            id = id,
            grid = grid,
            createdAt = created_at
        )
    }
}
