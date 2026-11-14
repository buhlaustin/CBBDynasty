package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.GameConstants
import dev.novux.dynasty.cbb.core.schedule.PreseasonSchedulePlan
import dev.novux.dynasty.cbb.core.schedule.ScheduledGameStub

/**
 * User calendar tab rows — week labels + byes ([planning/game-frame.md#calendar-tab]).
 */
object UserCalendarTabBuilder {
    fun build(
        activePosition: CalendarPosition,
        plan: PreseasonSchedulePlan?,
        exhibitionCount: Int,
        conferenceTourneyByeWeeks: List<Int> = emptyList(),
        conferenceTourneyGamesByWeek: Map<Int, List<ScheduledGameStub>> = emptyMap(),
        conferenceTourneyQualified: Boolean = true,
        conferenceTourneySimmed: Boolean = false,
        eventEliminationByeWeeks: Map<Int, String> = emptyMap(),
        conferenceTourneyStructuralByeConferenceName: String? = null,
        leagueMaxConfTourneyWeeks: Int? = null,
        userSkipsNcbaOpeningRound: Boolean = false,
    ): List<UserCalendarWeekRow> {
        if (plan == null) {
            return listOf(
                idleWeek(
                    activePosition,
                    activePosition,
                ),
            )
        }
        val rows = mutableListOf<UserCalendarWeekRow>()
        rows += exhibitionWeeks(activePosition, plan, exhibitionCount)
        rows += regularSeasonWeeks(activePosition, plan, eventEliminationByeWeeks)
        rows += conferenceTournamentWeeks(
            activePosition,
            conferenceTourneyByeWeeks,
            conferenceTourneyGamesByWeek,
            conferenceTourneyQualified,
            conferenceTourneySimmed,
            conferenceTourneyStructuralByeConferenceName,
            leagueMaxConfTourneyWeeks,
        )
        rows += postseasonWeeks(activePosition, userSkipsNcbaOpeningRound)
        return rows
    }

    private fun idleWeek(active: CalendarPosition, position: CalendarPosition): UserCalendarWeekRow =
        UserCalendarWeekRow(
            weekLabel = CalendarWeekLabel.display(position),
            phase = position.phase,
            weekIndex = position.weekIndex,
            rowKind = UserCalendarRowKind.OffWeek,
            games = emptyList(),
            isCurrentWeek = position == active,
        )

    private fun exhibitionWeeks(
        active: CalendarPosition,
        plan: PreseasonSchedulePlan,
        exhibitionCount: Int,
    ): List<UserCalendarWeekRow> {
        val capped = exhibitionCount.coerceIn(0, plan.exhibitions.size)
        return (1..PhaseWeekPolicy.weeksInPhase(CalendarPhase.Exhibition)).map { week ->
            val game = plan.exhibitions.getOrNull(week - 1)?.takeIf { week <= capped }
            val position = CalendarPosition(active.seasonIndex, CalendarPhase.Exhibition, week)
            weekRow(active, position, game?.let { listOf(it) }.orEmpty())
        }
    }

    private fun regularSeasonWeeks(
        active: CalendarPosition,
        plan: PreseasonSchedulePlan,
        eventEliminationByeWeeks: Map<Int, String>,
    ): List<UserCalendarWeekRow> {
        val byWeek = plan.regularSeason.groupBy { it.rsWeekIndex ?: 0 }
        return (1..GameConstants.MAX_REGULAR_SEASON_GAMES).map { week ->
            val games = byWeek[week].orEmpty()
            val position = CalendarPosition(active.seasonIndex, CalendarPhase.RegularSeason, week)
            if (games.isEmpty()) {
                val eventName = eventEliminationByeWeeks[week]
                if (eventName != null) {
                    return@map eventEliminationByeRow(active, position, eventName)
                }
            }
            weekRow(active, position, games)
        }
    }

    private fun conferenceTournamentWeeks(
        active: CalendarPosition,
        structuralByeWeeks: List<Int>,
        gamesByWeek: Map<Int, List<ScheduledGameStub>>,
        qualified: Boolean,
        bracketSimmed: Boolean,
        structuralByeConferenceName: String?,
        leagueMaxConfTourneyWeeks: Int?,
    ): List<UserCalendarWeekRow> {
        val max = PhaseWeekPolicy.weeksInPhase(CalendarPhase.ConferenceTournament)
        return (1..max).map { week ->
            val position = CalendarPosition(active.seasonIndex, CalendarPhase.ConferenceTournament, week)
            when {
                week in structuralByeWeeks ->
                    confTournamentStructuralByeRow(
                        active,
                        position,
                        structuralByeConferenceName,
                        leagueMaxConfTourneyWeeks,
                    )
                !qualified -> byeRow(active, position, leagueMaxConfTourneyWeeks)
                gamesByWeek[week].orEmpty().isNotEmpty() ->
                    weekRow(active, position, gamesByWeek[week].orEmpty(), leagueMaxConfTourneyWeeks)
                bracketSimmed -> byeRow(active, position, leagueMaxConfTourneyWeeks)
                else -> openWeekRow(active, position, leagueMaxConfTourneyWeeks)
            }
        }
    }

    private fun postseasonWeeks(
        active: CalendarPosition,
        userSkipsNcbaOpeningRound: Boolean,
    ): List<UserCalendarWeekRow> {
        val max = PhaseWeekPolicy.weeksInPhase(CalendarPhase.Postseason)
        return (1..max).map { week ->
            val position = CalendarPosition(active.seasonIndex, CalendarPhase.Postseason, week)
            if (week == PostseasonCalendar.OPENING_ROUND_WEEK && userSkipsNcbaOpeningRound) {
                ncbaOpeningRoundByeRow(active, position)
            } else {
                openWeekRow(active, position)
            }
        }
    }

    private fun openWeekRow(
        active: CalendarPosition,
        position: CalendarPosition,
        leagueMaxConfTourneyWeeks: Int? = null,
    ): UserCalendarWeekRow =
        UserCalendarWeekRow(
            weekLabel = CalendarWeekLabel.display(position, leagueMaxConfTourneyWeeks),
            phase = position.phase,
            weekIndex = position.weekIndex,
            rowKind = UserCalendarRowKind.OffWeek,
            games = emptyList(),
            isCurrentWeek = position == active,
        )

    private fun weekRow(
        active: CalendarPosition,
        position: CalendarPosition,
        games: List<ScheduledGameStub>,
        leagueMaxConfTourneyWeeks: Int? = null,
    ): UserCalendarWeekRow {
        val isPast = isPastWeek(active, position)
        if (games.isEmpty()) {
            return byeRow(active, position, leagueMaxConfTourneyWeeks)
        }
        return UserCalendarWeekRow(
            weekLabel = CalendarWeekLabel.display(position, leagueMaxConfTourneyWeeks),
            phase = position.phase,
            weekIndex = position.weekIndex,
            rowKind = UserCalendarRowKind.Game,
            games = games.mapIndexed { index, stub ->
                UserCalendarGameEntry(
                    opponentTeamId = stub.opponentTeamId,
                    opponentDisplayName = stub.opponentDisplayName,
                    homeGame = stub.homeGame,
                    gameKind = stub.kind,
                    isPastWeek = isPast,
                    gameDateLabel = CalendarWeekDisplayDate.gameDateLabel(position).orEmpty(),
                    gamePosition = position,
                    gameIndexInWeek = index,
                )
            },
            isCurrentWeek = position == active,
        )
    }

    private fun byeRow(
        active: CalendarPosition,
        position: CalendarPosition,
        leagueMaxConfTourneyWeeks: Int? = null,
    ): UserCalendarWeekRow =
        UserCalendarWeekRow(
            weekLabel = CalendarWeekLabel.display(position, leagueMaxConfTourneyWeeks),
            phase = position.phase,
            weekIndex = position.weekIndex,
            rowKind = UserCalendarRowKind.Bye,
            games = emptyList(),
            isCurrentWeek = position == active,
        )

    private fun eventEliminationByeRow(
        active: CalendarPosition,
        position: CalendarPosition,
        eventName: String,
    ): UserCalendarWeekRow =
        UserCalendarWeekRow(
            weekLabel = CalendarWeekLabel.display(position),
            phase = position.phase,
            weekIndex = position.weekIndex,
            rowKind = UserCalendarRowKind.EventEliminationBye,
            games = emptyList(),
            isCurrentWeek = position == active,
            eventEliminationByeEventName = eventName,
        )

    private fun confTournamentStructuralByeRow(
        active: CalendarPosition,
        position: CalendarPosition,
        conferenceName: String?,
        leagueMaxConfTourneyWeeks: Int?,
    ): UserCalendarWeekRow =
        UserCalendarWeekRow(
            weekLabel = CalendarWeekLabel.display(position, leagueMaxConfTourneyWeeks),
            phase = position.phase,
            weekIndex = position.weekIndex,
            rowKind = UserCalendarRowKind.ConfTournamentStructuralBye,
            games = emptyList(),
            isCurrentWeek = position == active,
            confTournamentStructuralByeConferenceName = conferenceName,
        )

    private fun ncbaOpeningRoundByeRow(
        active: CalendarPosition,
        position: CalendarPosition,
    ): UserCalendarWeekRow =
        UserCalendarWeekRow(
            weekLabel = CalendarWeekLabel.display(position),
            phase = position.phase,
            weekIndex = position.weekIndex,
            rowKind = UserCalendarRowKind.NcbaOpeningRoundBye,
            games = emptyList(),
            isCurrentWeek = position == active,
        )

    private fun isPastWeek(active: CalendarPosition, row: CalendarPosition): Boolean {
        if (row.seasonIndex != active.seasonIndex) return row.seasonIndex < active.seasonIndex
        if (row.phase != active.phase) return row.phase.ordinal < active.phase.ordinal
        return row.weekIndex < active.weekIndex
    }
}
