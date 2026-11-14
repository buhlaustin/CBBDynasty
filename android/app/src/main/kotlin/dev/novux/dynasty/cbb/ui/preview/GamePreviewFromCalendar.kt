package dev.novux.dynasty.cbb.ui.preview

import android.content.Context
import dev.novux.dynasty.cbb.core.calendar.UserCalendarGameEntry
import dev.novux.dynasty.cbb.core.preview.GamePreviewEngine
import dev.novux.dynasty.cbb.core.preview.GamePreviewRequest
import dev.novux.dynasty.cbb.core.preview.GamePreviewScoutingBuilder
import dev.novux.dynasty.cbb.core.preview.GamePreviewVenue
import dev.novux.dynasty.cbb.core.preview.HeadToHeadHistory
import dev.novux.dynasty.cbb.core.preview.HeadToHeadHistoryResolver
import dev.novux.dynasty.cbb.core.preview.TeamPreviewSide
import dev.novux.dynasty.cbb.core.schedule.ScheduledGameStub
import dev.novux.dynasty.cbb.core.school.SchoolHubOverview
import dev.novux.dynasty.cbb.requireDynastyApplication
object GamePreviewFromCalendar {
    suspend fun launch(
        context: Context,
        dynastyId: String,
        seasonIndex: Int,
        userSchool: SchoolHubOverview,
        game: UserCalendarGameEntry,
    ) {
        val app = context.requireDynastyApplication()
        val oppOverview = if (!game.opponentTeamId.startsWith("event-")) {
            app.schoolHubRepository.loadOpponentOverview(
                dynastyId,
                game.opponentTeamId,
                seasonIndex,
            )
        } else {
            null
        }
        launchFromStub(context, dynastyId, seasonIndex, userSchool, game.toStub(), oppOverview)
    }

    suspend fun launchFromStub(
        context: Context,
        dynastyId: String,
        seasonIndex: Int,
        userSchool: SchoolHubOverview,
        game: ScheduledGameStub,
        oppOverview: SchoolHubOverview?,
    ) {
        val app = context.requireDynastyApplication()
        val userTeamId = userSchool.teamId
        val opponent = oppOverview?.let { TeamPreviewSide.fromOverview(it) }
            ?: TeamPreviewSide.fromPrestige(
                game.opponentTeamId,
                game.opponentDisplayName,
                58,
            )
        val venue = if (game.homeGame) GamePreviewVenue.Home else GamePreviewVenue.Away
        val seasonResultsByIndex = buildMap {
            for (season in 1..seasonIndex) {
                val st = app.leagueWeekBatchRepository.loadStandings(dynastyId, season) ?: continue
                if (st.seasonResults.isNotEmpty()) put(season, st.seasonResults)
            }
        }
        val meetings = HeadToHeadHistoryResolver.collectDynastyMeetings(
            userTeamId = userTeamId,
            opponentTeamId = game.opponentTeamId,
            seasonResultsByIndex = seasonResultsByIndex,
        )
        val currentStandings = app.leagueWeekBatchRepository.loadStandings(dynastyId, seasonIndex)
        val preview = GamePreviewEngine.predict(
            GamePreviewRequest(
                user = TeamPreviewSide.fromOverview(userSchool),
                opponent = opponent,
                venue = venue,
                headToHead = HeadToHeadHistory.summarize(meetings),
            ),
        ).copy(
            userScouting = GamePreviewScoutingBuilder.fromOverview(userSchool, currentStandings),
            opponentScouting = oppOverview?.let { GamePreviewScoutingBuilder.fromOverview(it, currentStandings) }
                ?: GamePreviewScoutingBuilder.fromPrestige(
                    game.opponentTeamId,
                    game.opponentDisplayName,
                    58,
                ),
        )
        val loc = if (game.homeGame) {
            context.getString(dev.novux.dynasty.cbb.R.string.dynasty_shell_home)
        } else {
            context.getString(dev.novux.dynasty.cbb.R.string.dynasty_shell_away)
        }
        GamePreviewLaunch.matchupLabel = context.getString(
            dev.novux.dynasty.cbb.R.string.dynasty_shell_matchup,
            game.opponentDisplayName,
            loc,
        )
        GamePreviewLaunch.userTeamName = userSchool.displayName
        GamePreviewLaunch.opponentName = game.opponentDisplayName
        GamePreviewLaunch.venue = venue
        GamePreviewLaunch.venuePhrase = if (game.homeGame) {
            context.getString(dev.novux.dynasty.cbb.R.string.game_preview_venue_phrase_home)
        } else {
            context.getString(dev.novux.dynasty.cbb.R.string.game_preview_venue_phrase_away)
        }
        GamePreviewLaunch.result = preview
    }

    private fun UserCalendarGameEntry.toStub() = ScheduledGameStub(
        kind = gameKind,
        opponentTeamId = opponentTeamId,
        opponentDisplayName = opponentDisplayName,
        homeGame = homeGame,
        rsWeekIndex = gamePosition.weekIndex.takeIf { gamePosition.phase == dev.novux.dynasty.cbb.core.calendar.CalendarPhase.RegularSeason },
    )
}
