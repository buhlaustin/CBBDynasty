package dev.novux.dynasty.cbb.data.calendar

import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.calendar.ConferenceTournamentCalendar
import dev.novux.dynasty.cbb.core.calendar.EarlySeasonUserCalendarByes
import dev.novux.dynasty.cbb.core.calendar.UserCalendarGameEnricher
import dev.novux.dynasty.cbb.core.calendar.PostseasonUserCalendar
import dev.novux.dynasty.cbb.core.calendar.UserCalendarTabBuilder
import dev.novux.dynasty.cbb.core.calendar.UserCalendarWeekRow
import dev.novux.dynasty.cbb.data.dashboard.DashboardLastGameRepository
import dev.novux.dynasty.cbb.core.conference.ConferenceTournamentQualifier
import dev.novux.dynasty.cbb.core.conference.ConferenceTournamentUserCalendar
import dev.novux.dynasty.cbb.core.league.LeagueTeamRecord
import dev.novux.dynasty.cbb.data.league.LeagueWeekBatchRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase

class UserCalendarRepository(
    private val scheduleRepository: dev.novux.dynasty.cbb.data.preseason.PreseasonScheduleRepository,
    private val packRepository: PackRepository,
    private val leagueWeekBatchRepository: LeagueWeekBatchRepository,
    private val database: DynastyDatabase,
    private val weekCheckpointRepository: dev.novux.dynasty.cbb.data.calendar.WeekCheckpointRepository,
    private val dashboardLastGameRepository: DashboardLastGameRepository,
    private val earlySeasonBracketRepository: dev.novux.dynasty.cbb.data.preseason.EarlySeasonBracketRepository,
) {
    suspend fun loadCalendarRows(
        dynastyId: String,
        seasonIndex: Int,
        activePosition: CalendarPosition,
    ): List<UserCalendarWeekRow> {
        val plan = scheduleRepository.loadPlan(dynastyId, seasonIndex) ?: return emptyList()
        val exhibitionCount = scheduleRepository.loadExhibitionCount(dynastyId, seasonIndex)
        val coach = database.dynastyCoachDao().getByDynastyId(dynastyId)
        val session = packRepository.activeSession()
        val index = session.index
        val (confByeWeeks, confDisplayName) = if (coach != null) {
            val team = index.team(coach.userTeamId)
            if (team != null) {
                val byes = ConferenceTournamentCalendar.byeWeeksForConference(index.source, team.conferenceId)
                val name = index.conference(team.conferenceId)?.displayName
                byes to name
            } else {
                emptyList<Int>() to null
            }
        } else {
            emptyList<Int>() to null
        }
        val standings = leagueWeekBatchRepository.loadStandings(dynastyId, seasonIndex)
        val confContext = if (coach != null) {
            buildConferenceCalendarContext(
                coach.userTeamId,
                standings?.records.orEmpty(),
                standings?.conferenceTournaments,
            )
        } else {
            ConferenceCalendarContext()
        }
        val userTeamId = coach?.userTeamId
        val eventEliminationByes = if (userTeamId != null) {
            val bracketSeason = earlySeasonBracketRepository.load(dynastyId, seasonIndex)
            EarlySeasonUserCalendarByes.eliminationByeWeekLabels(bracketSeason, userTeamId)
        } else {
            emptyMap()
        }
        val leagueMaxConfWeeks = ConferenceTournamentCalendar.leagueMaxConfTourneyWeeks(index.source)
        val skipsOpeningRound = if (userTeamId != null) {
            PostseasonUserCalendar.skipsNcbaOpeningRound(standings?.selectionSunday, userTeamId)
        } else {
            false
        }
        val built = UserCalendarTabBuilder.build(
            activePosition = activePosition,
            plan = plan,
            exhibitionCount = exhibitionCount,
            conferenceTourneyByeWeeks = confByeWeeks,
            conferenceTourneyGamesByWeek = confContext.gamesByWeek,
            conferenceTourneyQualified = confContext.qualified,
            conferenceTourneySimmed = confContext.bracketSimmed,
            eventEliminationByeWeeks = eventEliminationByes,
            conferenceTourneyStructuralByeConferenceName = confDisplayName,
            leagueMaxConfTourneyWeeks = leagueMaxConfWeeks,
            userSkipsNcbaOpeningRound = skipsOpeningRound,
        )
        if (userTeamId == null) return built
        val checkpoint = weekCheckpointRepository.loadCheckpoint(dynastyId)
        val lastGame = dashboardLastGameRepository.load(dynastyId, seasonIndex)
        return UserCalendarGameEnricher.enrichRows(
            rows = built,
            userTeamId = userTeamId,
            activePosition = activePosition,
            standings = standings,
            provisionalGames = checkpoint?.provisionalGames.orEmpty(),
            lastGame = lastGame,
        )
    }

    private suspend fun buildConferenceCalendarContext(
        userTeamId: String,
        records: Map<String, LeagueTeamRecord>,
        season: dev.novux.dynasty.cbb.core.conference.ConferenceTournamentSeason?,
    ): ConferenceCalendarContext {
        val session = packRepository.activeSession()
        val index = session.index
        val team = index.team(userTeamId) ?: return ConferenceCalendarContext()
        val confId = team.conferenceId
        val conf = index.conference(confId) ?: return ConferenceCalendarContext()
        val templateId = conf.tournamentTemplateId ?: return ConferenceCalendarContext()
        val template = index.tournamentTemplate(templateId) ?: return ConferenceCalendarContext()
        val confTeams = index.teamsInConference(confId).map { it.teamId }
        val confRecords = confTeams.associateWith { records[it] ?: LeagueTeamRecord(it) }
        val templateQualified = ConferenceTournamentQualifier.qualify(template, confTeams, confRecords)
        val result = season?.resultsByConference?.get(confId)
        val qualified = ConferenceTournamentUserCalendar.userQualified(result, templateQualified, userTeamId)
        val gamesByWeek = if (result != null) {
            ConferenceTournamentUserCalendar.userGamesByWeek(
                result,
                userTeamId,
            ) { opponentId -> index.team(opponentId)?.displayName ?: opponentId }
        } else {
            emptyMap()
        }
        return ConferenceCalendarContext(
            qualified = qualified,
            bracketSimmed = result != null,
            gamesByWeek = gamesByWeek,
        )
    }

    private data class ConferenceCalendarContext(
        val qualified: Boolean = true,
        val bracketSimmed: Boolean = false,
        val gamesByWeek: Map<Int, List<dev.novux.dynasty.cbb.core.schedule.ScheduledGameStub>> = emptyMap(),
    )
}
