package dev.novux.dynasty.cbb.core.news

import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.school.SchoolNetRankIndex

/** Weekly NET / synthetic poll movement lines for Stories ([MEGA-SHELL-137]). */
object PollNetMovementNewsEngine {
    const val MEGA_ID: String = "MEGA-SHELL-137"

    fun headlineForWeek(
        standings: LeagueStandingsState,
        rsWeekIndex: Int,
        teamDisplayName: (String) -> String,
        userTeamId: String? = null,
    ): NewsHeadline? {
        val ranks = standings.netRankByTeam
        if (ranks.isEmpty() || rsWeekIndex < 2) return null
        val leader = ranks.entries.minByOrNull { it.value } ?: return null
        val leaderName = teamDisplayName(leader.key)
        val mediaTop = SchoolNetRankIndex.mediaPollRank(leader.value)
        val userLine = userTeamId?.let { id ->
            ranks[id]?.let { net ->
                val coaches = SchoolNetRankIndex.coachesPollRank(net)
                val media = SchoolNetRankIndex.mediaPollRank(net)
                "Your program: NET #$net, media #$media, coaches #$coaches."
            }
        }
        val title = buildString {
            append("NET & polls (Week $rsWeekIndex): ")
            append(leaderName)
            append(" leads NET #")
            append(leader.value)
            append(" (media poll ~#")
            append(mediaTop)
            append(")")
            userLine?.let {
                append(" ")
                append(it)
            }
        }
        return NewsHeadline(
            id = "poll-net-${standings.seasonIndex}-w$rsWeekIndex",
            seasonIndex = standings.seasonIndex,
            rsWeekIndex = rsWeekIndex,
            kind = NewsHeadlineKind.PollNetMovement,
            title = title,
            priority = 38,
            createdAtMs = System.currentTimeMillis(),
        )
    }
}