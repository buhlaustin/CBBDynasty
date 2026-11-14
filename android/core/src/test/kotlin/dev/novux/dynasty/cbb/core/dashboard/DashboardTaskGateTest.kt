package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.balance.BalanceMedicalRedshirtRules
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.dashboard.InSeasonRecruitingDashboardContext
import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId
import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskProgress
import dev.novux.dynasty.cbb.core.schedule.PreseasonSchedulePlan
import dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind
import dev.novux.dynasty.cbb.core.schedule.ScheduledGameStub
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardTaskGateTest {
    @Test
    fun preseasonBlocksUntilEightComplete() {
        val snapshot = DashboardTaskGate.preseason(PreseasonTaskProgress())
        assertTrue(DashboardAdvanceGate.blocksWeekAdvance(snapshot.required))
        assertEquals(PreseasonTaskId.entries.size, snapshot.required.size)
    }

    @Test
    fun inSeasonGameOptionalDoesNotBlock() {
        val plan = PreseasonSchedulePlan(
            exhibitions = emptyList(),
            regularSeason = listOf(
                ScheduledGameStub(
                    ScheduleGameKind.NonConference,
                    "opp",
                    "Opponent",
                    homeGame = true,
                    rsWeekIndex = 3,
                ),
            ),
            conferenceGameCount = 0,
            nonConferenceGameCount = 1,
            earlyEventGameCount = 0,
        )
        val snapshot = DashboardTaskGate.inSeason(
            position = CalendarPosition(1, CalendarPhase.RegularSeason, 3),
            plan = plan,
            exhibitionCount = 0,
            weekSimStaged = false,
            disciplinePending = null,
            rosterPlayers = emptyList(),
            medicalRules = BalanceMedicalRedshirtRules(),
        )
        assertFalse(DashboardAdvanceGate.blocksWeekAdvance(snapshot.required))
        assertTrue(snapshot.optional.any { it.id == DashboardTaskId.PlayScheduledGame })
    }

    @Test
    fun stagedSimHidesPlayTask() {
        val plan = PreseasonSchedulePlan(
            exhibitions = emptyList(),
            regularSeason = listOf(
                ScheduledGameStub(ScheduleGameKind.Conference, "x", "X", true, 1),
            ),
            conferenceGameCount = 1,
            nonConferenceGameCount = 0,
            earlyEventGameCount = 0,
        )
        val snapshot = DashboardTaskGate.inSeason(
            CalendarPosition(1, CalendarPhase.RegularSeason, 1),
            plan,
            0,
            weekSimStaged = true,
            null,
            emptyList(),
            BalanceMedicalRedshirtRules(),
        )
        assertTrue(snapshot.optional.none { it.id == DashboardTaskId.PlayScheduledGame })
        assertTrue(snapshot.optional.any { it.id == DashboardTaskId.ReplayStagedWeekGame })
    }

    @Test
    fun inSeasonRecruitingSpendOptionalWhenLedgerHasPoints() {
        val snapshot = DashboardTaskGate.inSeason(
            CalendarPosition(2, CalendarPhase.RegularSeason, 4),
            null,
            0,
            false,
            null,
            emptyList(),
            BalanceMedicalRedshirtRules(),
            recruiting = InSeasonRecruitingDashboardContext(
                phase = CalendarPhase.RegularSeason,
                pointsAvailable = 150,
                cycleClosed = false,
            ),
        )
        assertTrue(snapshot.optional.any { it.id == DashboardTaskId.SpendRecruitingPoints })
    }

    @Test
    fun coachSundayNudgeWhenBankPositive() {
        val snapshot = DashboardTaskGate.inSeason(
            CalendarPosition(1, CalendarPhase.RegularSeason, 2),
            null,
            0,
            false,
            null,
            emptyList(),
            BalanceMedicalRedshirtRules(),
            coachSkillPointBank = 1,
        )
        assertTrue(snapshot.optional.any { it.id == DashboardTaskId.CoachSundaySkillPoints })
    }
}
