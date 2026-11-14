package dev.novux.dynasty.cbb.core.calendar.checkpoint

import dev.novux.dynasty.cbb.core.sim.GameSimResult

/**
 * In-week sim result staged before [WeekCheckpointState.advanceWeek].
 * Box archive persists possession-sim team and player lines until week commit.
 */
data class ProvisionalGameMark(
    val gameId: String,
    val homeScore: Int,
    val awayScore: Int,
    val simGeneration: Int = 1,
    val possessionEngine: Boolean = false,
    val boxArchive: ProvisionalGameBoxArchive? = null,
) {
    init {
        require(gameId.isNotBlank()) { "gameId required" }
        require(homeScore >= 0 && awayScore >= 0) { "scores must be non-negative" }
        require(simGeneration >= 1) { "simGeneration must be >= 1" }
    }

    companion object {
        fun fromGameSim(
            gameId: String,
            result: GameSimResult,
            userTeamIsHome: Boolean,
            previousGeneration: Int = 0,
        ): ProvisionalGameMark {
            val generation = if (previousGeneration > 0) previousGeneration + 1 else 1
            return ProvisionalGameMark(
                gameId = gameId,
                homeScore = result.homeScore,
                awayScore = result.awayScore,
                simGeneration = generation,
                possessionEngine = true,
                boxArchive = ProvisionalGameBoxArchive.fromResult(result, userTeamIsHome),
            )
        }
    }
}
