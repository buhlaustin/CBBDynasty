package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.IntentionalFoulLateGamePath
import dev.novux.dynasty.cbb.core.strategy.LateGamePossessionPolicy

/**
 * Explicit intentional foul branch when defense chose foul-early (MEGA-SIM-106).
 */
object IntentionalFoulLateGamePolicy {
    val QA_ID: String = IntentionalFoulLateGamePath.SIM_ID

    fun isIntentionalFoulDecision(decision: LateGamePossessionPolicy.PossessionDecision): Boolean =
        decision == LateGamePossessionPolicy.PossessionDecision.IntentionalFoulTrip

    /** No FGA/turnover/scoring terminal — only clock + team foul + bonus FTs. */
    fun resolvePossession(offenseBox: TeamBoxScore): PossessionResolveResult =
        PossessionResolveResult(
            box = offenseBox.copy(possessions = offenseBox.possessions + 1),
            change = PossessionChange.OffenseRetainsOffensiveRebound,
            subPhaseChain = "intentional foul",
        )

    fun foulLogLabel(personalFoulLine: String?): String =
        personalFoulLine?.let { "intentional foul | $it" } ?: "intentional foul"
}
