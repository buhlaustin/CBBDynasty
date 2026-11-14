package dev.novux.dynasty.cbb.core.schedule

import dev.novux.dynasty.cbb.core.preseason.tournament.RegularSeasonSlotBudget
import org.junit.Assert.assertEquals
import org.junit.Test

class RegularSeasonScheduleBuilderUxPolicyTest {
    @Test
    fun previewSlotSummary_countsNonConferenceSlots() {
        val context = RegularSeasonScheduleBuilderUxPolicy.BuilderContext(
            budget = RegularSeasonSlotBudget(eventRsGames = 4, builderSlots = 28),
            conferenceTemplateGames = 18,
            exhibitionCount = 2,
        )
        val summary = RegularSeasonScheduleBuilderUxPolicy.previewSlotSummary(context)
        assertEquals(10, summary.nonConferenceSlots)
        assertEquals(18, summary.conferenceLocked)
    }

    @Test
    fun gamesGroupedByWeek_sortsAscending() {
        val plan = PreseasonSchedulePlan(
            exhibitions = emptyList(),
            regularSeason = listOf(
                stub(ScheduleGameKind.NonConference, week = 5),
                stub(ScheduleGameKind.Conference, week = 2),
            ),
            conferenceGameCount = 1,
            nonConferenceGameCount = 1,
            earlyEventGameCount = 0,
        )
        assertEquals(listOf(2, 5), RegularSeasonScheduleBuilderUxPolicy.gamesGroupedByWeek(plan).keys.toList())
    }

    @Test
    fun megaFrameId() {
        assertEquals("MEGA-FRAME-076", RegularSeasonScheduleBuilderUxPolicy.MEGA_FRAME_ID)
    }

    private fun stub(kind: ScheduleGameKind, week: Int) = ScheduledGameStub(
        kind = kind,
        opponentTeamId = "opp",
        opponentDisplayName = "Opp",
        homeGame = true,
        rsWeekIndex = week,
    )
}
