package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameMark
import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState

/**
 * User league games on week advance — possession engine, not random score stubs
 * ([MEGA-SIM-175]).
 */
object UserLeagueGameSimulator {
    data class Outcome(
        val mark: ProvisionalGameMark,
        val userTeamIsHome: Boolean,
    )

    fun fastSimUnplayed(
        state: WeekCheckpointState,
        bundle: UserLeagueFastSimBundle?,
        unplayedCount: Int,
    ): Pair<WeekCheckpointState, List<Outcome>> {
        if (unplayedCount <= 0 || state.provisionalGames.isNotEmpty()) {
            return state to emptyList()
        }
        val contexts = bundle?.contexts.orEmpty()
        require(contexts.size == unplayedCount) {
            "Expected $unplayedCount user league sim context(s), got ${contexts.size}"
        }
        var next = state
        val outcomes = mutableListOf<Outcome>()
        contexts.forEach { ctx ->
            val priorGen = next.provisionalGames[ctx.gameId]?.simGeneration ?: 0
            val result = ctx.simulate()
            val mark = ProvisionalGameMark.fromGameSim(
                gameId = ctx.gameId,
                result = result,
                userTeamIsHome = ctx.userTeamIsHome,
                previousGeneration = priorGen,
            )
            next = next.stageSim(mark)
            outcomes += Outcome(mark, ctx.userTeamIsHome)
        }
        return next to outcomes
    }
}
