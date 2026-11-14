package dev.novux.dynasty.cbb.core.calendar.checkpoint

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.save.DynastySaveValidator
import org.junit.Assert.assertNull
import org.junit.Test

/** Load safety after week advance — no invalid calendar snapshots ([MEGA-QA-028]). */
class WeekCheckpointAdvanceRecoveryTest {
    @Test
    fun advanceWeek_producesCalendarValidForLoadValidator() {
        val initial = WeekCheckpointState.bootstrap(seasonIndex = 1, phase = CalendarPhase.RegularSeason)
        val mark = ProvisionalGameMark(
            gameId = "game-1",
            homeScore = 70,
            awayScore = 65,
            simGeneration = 1,
        )
        val staged = initial.stageSim(mark)
        val result = staged.advanceWeek()
        val snap = DynastySaveValidator.CalendarSnapshot(
            seasonIndex = result.nextState.activePosition.seasonIndex,
            phase = result.nextState.activePosition.phase,
            activeWeekIndex = result.nextState.activePosition.weekIndex,
            committedSeasonIndex = result.nextState.committedThrough?.seasonIndex,
            committedPhase = result.nextState.committedThrough?.phase,
            committedWeekIndex = result.nextState.committedThrough?.weekIndex,
        )
        assertNull(DynastySaveValidator.validateCalendar(snap))
    }

    @Test
    fun preAdvanceStaged_notWrittenUntilAdvance_commitsOnlyFinalState() {
        val initial = WeekCheckpointState.bootstrap(seasonIndex = 1, phase = CalendarPhase.RegularSeason)
        val mark = ProvisionalGameMark(
            gameId = "game-2",
            homeScore = 80,
            awayScore = 77,
            simGeneration = 1,
        )
        val staged = initial.stageSim(mark)
        assert(staged.provisionalGames.isNotEmpty())
        val advanced = staged.advanceWeek()
        assert(advanced.nextState.provisionalGames.isEmpty())
        assert(advanced.gamesCommitted.containsKey("game-2"))
    }
}
