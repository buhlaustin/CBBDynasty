package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.SimTeamFoulRules
import kotlin.random.Random

/**
 * Bonus / double-bonus free throws on non-shooting fouls when team foul limits are reached
 * ([planning/simulation-engine.md#what-can-happen-on-a-possession], MEGA-SIM-095).
 */
object TeamFoulBonusPolicy {
    const val QA_ID: String = "MEGA-SIM-095"

    enum class BonusState {
        None,
        Bonus,
        DoubleBonus,
    }

    fun stateAfterFoul(foulsInHalf: Int, rules: SimTeamFoulRules): BonusState =
        when {
            foulsInHalf >= rules.doubleBonusThreshold -> BonusState.DoubleBonus
            foulsInHalf >= rules.bonusThreshold -> BonusState.Bonus
            else -> BonusState.None
        }

    fun logTag(state: BonusState): String? =
        when (state) {
            BonusState.Bonus -> "bonus"
            BonusState.DoubleBonus -> "double bonus"
            BonusState.None -> null
        }

    fun sampleNonShootingFreeThrows(
        foulsInHalf: Int,
        rules: SimTeamFoulRules,
        random: Random,
    ): Pair<Int, Int>? =
        when (stateAfterFoul(foulsInHalf, rules)) {
            BonusState.None -> null
            BonusState.DoubleBonus ->
                PlayerPossessionStatRecorder.sampleFreeThrowChain(attempts = 2, random = random)
            BonusState.Bonus ->
                if (rules.oneAndOneUntilDouble) {
                    sampleOneAndOne(random)
                } else {
                    PlayerPossessionStatRecorder.sampleFreeThrowChain(attempts = 2, random = random)
                }
        }

    private fun sampleOneAndOne(random: Random): Pair<Int, Int> {
        val firstMade = random.nextDouble() < 0.74
        if (!firstMade) return 0 to 1
        val secondMade = random.nextDouble() < 0.74
        return ((1 + if (secondMade) 1 else 0) to 2)
    }
}
