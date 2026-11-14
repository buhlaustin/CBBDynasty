package dev.novux.dynasty.cbb.core.preview

import dev.novux.dynasty.cbb.core.calendar.SeasonMembershipYear
import dev.novux.dynasty.cbb.core.league.LeagueGameResult

/** Dynasty-era meetings between user and opponent — [planning/game-frame.md#game-preview-sub-menu]. */
object HeadToHeadHistoryResolver {
    fun meetingsForPair(
        userTeamId: String,
        opponentTeamId: String,
        seasonIndex: Int,
        results: List<LeagueGameResult>,
    ): List<HeadToHeadMeeting> {
        val label = SeasonMembershipYear.label(seasonIndex)
        return results
            .filter { game ->
                (game.homeTeamId == userTeamId && game.awayTeamId == opponentTeamId) ||
                    (game.homeTeamId == opponentTeamId && game.awayTeamId == userTeamId)
            }
            .map { game ->
                val userHome = game.homeTeamId == userTeamId
                HeadToHeadMeeting(
                    seasonLabel = label,
                    userScore = if (userHome) game.homeScore else game.awayScore,
                    opponentScore = if (userHome) game.awayScore else game.homeScore,
                )
            }
    }

    fun collectDynastyMeetings(
        userTeamId: String,
        opponentTeamId: String,
        seasonResultsByIndex: Map<Int, List<LeagueGameResult>>,
    ): List<HeadToHeadMeeting> =
        seasonResultsByIndex
            .toSortedMap()
            .flatMap { (seasonIndex, results) ->
                meetingsForPair(userTeamId, opponentTeamId, seasonIndex, results)
            }
}
