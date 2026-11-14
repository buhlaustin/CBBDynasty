package dev.novux.dynasty.cbb.core.rankings

import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.league.LeagueTeamRecord
import dev.novux.dynasty.cbb.core.pack.IdentityPackIndex
import dev.novux.dynasty.cbb.core.resume.HumanPollRankingTable
import dev.novux.dynasty.cbb.core.school.SchoolNetRankIndex

/** Rankings hub tabs — [planning/game-frame.md#rankings-hub]. */
object RankingsHubSnapshotFactory {
    fun build(
        packIndex: IdentityPackIndex,
        standings: LeagueStandingsState?,
        rankingsPublished: Boolean,
    ): RankingsHubSnapshot {
        val teams = packIndex.allTeamDisplays()
        val netRanks = standings?.netRankByTeam?.takeIf { it.isNotEmpty() }
            ?: SchoolNetRankIndex.build(teams)
        val mediaRanks = standings?.mediaPollRankByTeam?.takeIf { it.isNotEmpty() }
            ?: netRanks.mapValues { (_, rank) -> SchoolNetRankIndex.mediaPollRank(rank) }
        val coachesRanks = standings?.coachesPollRankByTeam?.takeIf { it.isNotEmpty() }
            ?: netRanks.mapValues { (_, rank) -> SchoolNetRankIndex.coachesPollRank(rank) }
        val net = toRankList(packIndex, netRanks, highlightTop25 = false)
        val media = toRankList(packIndex, mediaRanks, highlightTop25 = true)
        val coaches = toRankList(packIndex, coachesRanks, highlightTop25 = true)
        val conferences = conferenceTables(packIndex, standings)
        return RankingsHubSnapshot(
            published = rankingsPublished,
            net = net,
            media = media,
            coaches = coaches,
            conferences = conferences,
        )
    }

    private fun toRankList(
        packIndex: IdentityPackIndex,
        ranks: Map<String, Int>,
        highlightTop25: Boolean,
    ): List<RankingsHubTeamRow> =
        ranks.entries
            .sortedWith(compareBy({ it.value }, { it.key }))
            .map { (teamId, rank) ->
                RankingsHubTeamRow(
                    teamId = teamId,
                    displayName = packIndex.team(teamId)?.displayName ?: teamId,
                    rank = rank,
                    top25Highlight = highlightTop25 && rank <= HumanPollRankingTable.TOP_25_CUTOFF,
                )
            }

    private fun conferenceTables(
        packIndex: IdentityPackIndex,
        standings: LeagueStandingsState?,
    ): List<ConferenceStandingsGroup> =
        packIndex.allTeamDisplays()
            .groupBy { it.conferenceId }
            .map { (confId, confTeams) ->
                ConferenceStandingsGroup(
                    conferenceId = confId,
                    conferenceName = packIndex.conference(confId)?.displayName ?: confId,
                    teams = ConferenceStandingsTiebreakerEngine.sortConferenceTeams(
                        conferenceId = confId,
                        teams = confTeams,
                        records = standings?.records ?: emptyMap(),
                        seasonResults = standings?.seasonResults ?: emptyList(),
                        tiebreakers = packIndex.conference(confId)?.standingsTiebreakers ?: emptyList(),
                    ),
                )
            }
            .sortedBy { it.conferenceName }
}
