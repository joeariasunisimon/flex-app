package co.jarias.flexapp.data.repository

import co.jarias.flexapp.data.local.Database
import co.jarias.flexapp.domain.Game
import co.jarias.flexapp.domain.WinCondition
import co.jarias.flexapp.shared.database.Game as DbGame

class GameRepositoryImpl(private val database: Database) : GameRepository {

    private val queries = database.database.bingoDatabaseQueries

    override suspend fun getAllGames(): List<Game> {
        return queries.selectAllGames().executeAsList().map { it.toDomain() }
    }


    override suspend fun insertGame(game: Game): Long {
        val targetFigureStr = game.targetFigure?.let { WinCondition.serialize(it) }
        queries.insertGame(game.name, targetFigureStr, game.createdAt)
        return queries.selectAllGames().executeAsList().last().id // Not ideal, but for simplicity
    }

    override suspend fun updateGame(game: Game) {
        val targetFigureStr = game.targetFigure?.let { WinCondition.serialize(it) }
        game.id?.let { queries.updateGame(game.name, targetFigureStr, game.createdAt, it) }
    }

    override suspend fun updateGameCompletion(gameId: Long, isCompleted: Boolean, completedAt: String?) {
        queries.updateGameCompletion(if (isCompleted) 1L else 0L, completedAt, gameId)
    }

    override suspend fun deleteGame(gameId: Long) {
        queries.deleteGame(gameId)
    }

    override suspend fun getGameById(gameId: Long): Game? {
        return queries.selectGameById(gameId).executeAsOneOrNull()?.toDomain()
    }

    private fun DbGame.toDomain(): Game {
        return Game(
            id = id,
            name = name,
            targetFigure = target_figure?.let { figureStr ->
                try {
                    WinCondition.deserialize(figureStr)
                } catch (e: Exception) {
                    null
                }
            },
            createdAt = created_at,
            isCompleted = is_completed.toInt() == 1,
            completedAt = completed_at
        )
    }
}
