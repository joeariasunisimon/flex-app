package co.jarias.flexapp.domain.usecase

import co.jarias.flexapp.data.repository.GameRepository
import co.jarias.flexapp.domain.Game
import co.jarias.flexapp.domain.WinCondition
import kotlin.time.Clock

class RestartGameWithSameCardUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(oldGameId: Long, newTargetFigure: WinCondition? = null): Game? {
        val oldGame = gameRepository.getGameById(oldGameId) ?: return null
        val cardId = oldGame.cardId ?: return null

        val createdAt = Clock.System.now().toString()
        
        val newGame = Game(
            name = oldGame.name, 
            cardId = cardId,
            targetFigure = newTargetFigure,
            createdAt = createdAt
        )
        
        val newGameId = gameRepository.insertGame(newGame)
        
        return newGame.copy(id = newGameId)
    }
}
