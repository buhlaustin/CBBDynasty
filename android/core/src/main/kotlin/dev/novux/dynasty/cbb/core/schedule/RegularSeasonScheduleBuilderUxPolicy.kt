package dev.novux.dynasty.cbb.core.schedule

import dev.novux.dynasty.cbb.core.preseason.tournament.RegularSeasonSlotBudget

/** Preseason task 8 — RS schedule builder ([planning/preseason.md#8--schedule-regular-season-games], [MEGA-FRAME-076]). */
object RegularSeasonScheduleBuilderUxPolicy {
    const val MEGA_FRAME_ID: String = "MEGA-FRAME-076"

    data class BuilderContext(
        val budget: RegularSeasonSlotBudget,
        val conferenceTemplateGames: Int,
        val exhibitionCount: Int,
    )

    fun gamesGroupedByWeek(plan: PreseasonSchedulePlan): Map<Int, List<ScheduledGameStub>> =
        plan.regularSeason
            .filter { it.rsWeekIndex != null }
            .groupBy { requireNotNull(it.rsWeekIndex) }
            .toSortedMap()

    fun previewSlotSummary(context: BuilderContext): SlotSummary =
        SlotSummary(
            builderSlots = context.budget.builderSlots,
            totalRsCap = context.budget.totalRsCap,
            eventRsGames = context.budget.eventRsGames,
            conferenceLocked = context.conferenceTemplateGames.coerceAtMost(context.budget.builderSlots),
        )

    data class SlotSummary(
        val builderSlots: Int,
        val totalRsCap: Int,
        val eventRsGames: Int,
        val conferenceLocked: Int,
    ) {
        val nonConferenceSlots: Int = (builderSlots - conferenceLocked).coerceAtLeast(0)
    }
}

sealed class RegularSeasonScheduleSolveResult {
    data class Saved(val plan: PreseasonSchedulePlan) : RegularSeasonScheduleSolveResult()
    data class Invalid(val validation: PreseasonScheduleValidation) : RegularSeasonScheduleSolveResult()
    data object MissingContext : RegularSeasonScheduleSolveResult()
}
