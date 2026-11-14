package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.ClutchUsagePath
import dev.novux.dynasty.cbb.core.sim.tuning.SimClutchUsageWeights
import dev.novux.dynasty.cbb.core.strategy.GameplanUsageNormalizer

/**
 * Late close games concentrate touches on the gameplan peak usage player (MEGA-SIM-115).
 */
object ClutchUsagePolicy {
    val QA_ID: String = ClutchUsagePath.SIM_ID

    fun isClutch(
        gameClockSeconds: Int,
        offenseScore: Int,
        defenseScore: Int,
        weights: SimClutchUsageWeights,
    ): Boolean {
        if (gameClockSeconds > weights.lateWindowSeconds) return false
        return kotlin.math.abs(offenseScore - defenseScore) <= weights.closeMarginPoints
    }

    fun peakUsagePlayerId(usageWeights: Map<String, Int>): String? =
        usageWeights.maxByOrNull { it.value }?.key

    fun effectiveUsageWeights(
        baseWeights: Map<String, Int>,
        offenseFloor: List<String>,
        gameClockSeconds: Int,
        offenseScore: Int,
        defenseScore: Int,
        tuning: SimClutchUsageWeights,
    ): Map<String, Int> {
        if (!isClutch(gameClockSeconds, offenseScore, defenseScore, tuning)) return baseWeights
        val base = if (baseWeights.isEmpty()) {
            offenseFloor.associateWith { 20 }
        } else {
            baseWeights
        }
        val peakId = peakUsagePlayerId(base) ?: return base
        if (peakId !in offenseFloor) return base
        return base.mapValues { (id, weight) ->
            if (id == peakId) {
                (weight * tuning.peakPlayerUsageMultiplier).toInt().coerceAtLeast(1)
            } else {
                weight
            }
        }
    }

    fun peakShareAfterBump(weights: Map<String, Int>): Double {
        val normalized = GameplanUsageNormalizer.normalize(weights)
        val peak = peakUsagePlayerId(weights) ?: return 0.0
        return normalized[peak] ?: 0.0
    }

    fun logTag(active: Boolean, peakPlayerId: String?): String? =
        if (active && peakPlayerId != null) "clutch usage $peakPlayerId" else null
}
