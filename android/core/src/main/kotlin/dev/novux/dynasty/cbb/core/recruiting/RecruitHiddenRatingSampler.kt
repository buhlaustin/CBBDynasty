package dev.novux.dynasty.cbb.core.recruiting

import kotlin.random.Random

/**
 * Hidden 0–99 talent composite — sampled by **pool percentile rank**, not post-hoc from stars or
 * creation index ([planning/recruiting-engine.md#hidden-ratings--letter-grades], [MEGA-RECR-137]).
 */
object RecruitHiddenRatingSampler {
    fun forPercentileRank(rank: Int, poolSize: Int, random: Random): Int {
        val size = poolSize.coerceAtLeast(1)
        val clampedRank = rank.coerceIn(0, size - 1)
        val base = 99 - ((clampedRank * 97) / size)
        val jitter = random.nextInt(-2, 3)
        return (base + jitter).coerceIn(1, 99)
    }
}
