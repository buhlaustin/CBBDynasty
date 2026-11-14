package dev.novux.dynasty.cbb.core.preseason

import dev.novux.dynasty.cbb.core.rankings.RankingsHubTeamRow

/** Preseason task 4 — NET + polls review sheet ([planning/preseason.md#4--net--media--coaches-poll-review-preseason]). */
object PreseasonRankingsReviewUxPolicy {
    const val MEGA_FRAME_ID: String = "MEGA-FRAME-072"

    fun userProgramRanks(
        userTeamId: String?,
        net: List<RankingsHubTeamRow>,
        media: List<RankingsHubTeamRow>,
        coaches: List<RankingsHubTeamRow>,
    ): ProgramPollRanks? {
        val teamId = userTeamId ?: return null
        val netRank = net.firstOrNull { it.teamId == teamId }?.rank
        val mediaRank = media.firstOrNull { it.teamId == teamId }?.rank
        val coachesRank = coaches.firstOrNull { it.teamId == teamId }?.rank
        if (netRank == null && mediaRank == null && coachesRank == null) return null
        return ProgramPollRanks(netRank = netRank, mediaRank = mediaRank, coachesRank = coachesRank)
    }

    data class ProgramPollRanks(
        val netRank: Int?,
        val mediaRank: Int?,
        val coachesRank: Int?,
    )
}
