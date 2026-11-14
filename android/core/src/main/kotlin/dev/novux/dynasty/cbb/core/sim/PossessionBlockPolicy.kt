package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.SimEventWeights
import dev.novux.dynasty.cbb.core.sim.tuning.SimEventWeightsResolver
import kotlin.random.Random

/** Blocked shot attempt on miss path ([planning/simulation-engine.md], MEGA-SIM-093). */
object PossessionBlockPolicy {
    const val QA_ID: String = "MEGA-SIM-093"

    data class Outcome(
        val blocked: Boolean,
        val foulOnBlock: Boolean = false,
    ) {
        companion object {
            val None = Outcome(blocked = false)
        }
    }

    fun rollOnMiss(
        defRating: Int,
        offRating: Int,
        random: Random,
        eventWeights: SimEventWeights = SimEventWeightsResolver.DEFAULT,
    ): Outcome {
        val edge = (defRating - offRating).coerceIn(-20, 20)
        val block = eventWeights.ratingGapBlockOnMiss
        val rate = eventWeights.blockRateForEdge(edge)
        if (random.nextDouble() >= rate) return Outcome.None
        val foul = random.nextDouble() < block.foulOnBlock
        return Outcome(blocked = true, foulOnBlock = foul)
    }
}
