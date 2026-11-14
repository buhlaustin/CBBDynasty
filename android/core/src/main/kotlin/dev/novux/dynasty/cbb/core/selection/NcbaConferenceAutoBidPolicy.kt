package dev.novux.dynasty.cbb.core.selection

import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueTeamRecord
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.rankings.ConferenceStandingsTiebreakerEngine

/** Conference auto bids use standings tie stack before NET fallback ([MEGA-GDD-101]). */
object NcbaConferenceAutoBidPolicy {
    const val MEGA_ID: String = "MEGA-GDD-101"

    fun fromStandings(
        teams: List<TeamDisplayOverlay>,
        records: Map<String, LeagueTeamRecord>,
        seasonResults: List<LeagueGameResult>,
        tiebreakersByConference: (String) -> List<String>,
        netRankByTeam: Map<String, Int>,
    ): Set<String> =
        teams
            .groupBy { it.conferenceId }
            .mapNotNull { (confId, confTeams) ->
                val eligible = confTeams.filter { it.postseasonEligible }
                if (eligible.isEmpty()) return@mapNotNull null
                val sorted = ConferenceStandingsTiebreakerEngine.sortConferenceTeams(
                    conferenceId = confId,
                    teams = eligible,
                    records = records,
                    seasonResults = seasonResults,
                    tiebreakers = tiebreakersByConference(confId),
                )
                sorted.firstOrNull()?.teamId
                    ?: eligible.minByOrNull { netRankByTeam[it.teamId] ?: Int.MAX_VALUE }?.teamId
            }
            .toSet()
}