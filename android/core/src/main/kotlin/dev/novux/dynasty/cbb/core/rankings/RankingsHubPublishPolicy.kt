package dev.novux.dynasty.cbb.core.rankings

import dev.novux.dynasty.cbb.core.league.LeagueStandingsState

/** When full NET / poll tabs are available vs preseason placeholder ([planning/game-frame.md#rankings-hub]). */
object RankingsHubPublishPolicy {
    fun rankingsPublished(
        preseasonRankingsReviewComplete: Boolean,
        standings: LeagueStandingsState?,
    ): Boolean {
        if (preseasonRankingsReviewComplete) return true
        return standings?.netRankByTeam?.isNotEmpty() == true
    }
}
