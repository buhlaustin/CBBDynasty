package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.player.scout.ScoutDepth

/**
 * Assigns star bands from **hidden-rating sort order** within each pool — never creation index
 * ([planning/recruiting-engine.md#star-distribution], [MEGA-RECR-136]).
 */
object RecruitStarAssignmentEngine {
    fun assignStarsWithinPool(
        prospects: List<RecruitProspect>,
        jucoBias: Boolean,
    ): List<RecruitProspect> {
        if (prospects.isEmpty()) return emptyList()
        val size = prospects.size
        val sorted = prospects.sortedByDescending { it.hiddenRating }
        val starForHidden: Map<Int, Int> = sorted
            .mapIndexed { rank, prospect -> prospect.hiddenRating to rank }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, ranks) ->
                val bestRank = ranks.minOrNull() ?: 0
                IncomingClassSeeder.starBandForPercentile(bestRank.toDouble() / size)
            }
        return prospects.map { prospect ->
            val stars = starForHidden.getValue(prospect.hiddenRating)
            val letters = RecruitScoutLetterEngine.fromProspect(prospect, ScoutDepth.Light)
            prospect.copy(
                stars = stars,
                nilAskUsd = IncomingClassSeeder.nilAskForStars(stars, jucoBias),
                scoutLetterOverall = letters.overall,
            )
        }
    }
}
