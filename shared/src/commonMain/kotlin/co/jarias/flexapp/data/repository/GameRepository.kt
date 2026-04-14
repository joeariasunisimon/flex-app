package co.jarias.flexapp.data.repository

import co.jarias.flexapp.domain.Game

interface GameRepository {
    suspend fun getAllGames(): List<Game>
    suspend fun insertGame(game: Game): Long
    suspend fun updateGame(game: Game)
    suspend fun updateGameCompletion(gameId: Long, isCompleted: Boolean, completedAt: String?)
    suspend fun deleteGame(gameId: Long)
    suspend fun getGameById(gameId: Long): Game?
}
