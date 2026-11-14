package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.signingday.SigningDayConstants

/**
 * Year-long recruiting cycle — not offseason-only ([planning/recruiting-engine.md]).
 */
object RecruitingCyclePolicy {
    val inSeasonPhases: Set<CalendarPhase> = setOf(
        CalendarPhase.Preseason,
        CalendarPhase.Exhibition,
        CalendarPhase.RegularSeason,
        CalendarPhase.ConferenceTournament,
        CalendarPhase.Postseason,
    )

    fun hubAccessible(phase: CalendarPhase, offseasonWeekIndex: Int? = null): Boolean =
        when (phase) {
            // Year-long HS/JUCO hub — open retention week 1 ([planning/recruiting-engine.md], [MEGA-RECR-038]).
            CalendarPhase.Offseason -> (offseasonWeekIndex ?: 1) >= 1
            in inSeasonPhases -> true
            else -> false
        }

    /**
     * Ledger flag or offseason administration weeks after Signing Day (W7–10)
     * ([planning/offseason.md], [MEGA-FRAME-089]).
     */
    fun effectiveCycleClosed(
        phase: CalendarPhase,
        offseasonWeekIndex: Int?,
        ledgerCycleClosed: Boolean,
    ): Boolean =
        ledgerCycleClosed ||
            (
                phase == CalendarPhase.Offseason &&
                    offseasonWeekIndex != null &&
                    offseasonWeekIndex > SigningDayConstants.OFFSEASON_WEEK_INDEX
                )

    /** Full recruiting hub (chasing/spend) — blocked after hard sign closes the cycle ([MEGA-FRAME-090]). */
    fun hubNavigable(
        phase: CalendarPhase,
        offseasonWeekIndex: Int? = null,
        ledgerCycleClosed: Boolean,
    ): Boolean =
        hubAccessible(phase, offseasonWeekIndex) &&
            !effectiveCycleClosed(phase, offseasonWeekIndex, ledgerCycleClosed)

    /**
     * Hamburger / shell menus while the ledger is still loading: assume closed when the hub
     * would be reachable, so preseason does not flash an open Recruiting item ([MEGA-FRAME-090]).
     */
    fun recruitingMenuTreatsCycleClosed(
        phase: CalendarPhase,
        offseasonWeekIndex: Int?,
        ledger: RecruitingCycleLedger?,
    ): Boolean =
        when (ledger) {
            null -> hubAccessible(phase, offseasonWeekIndex)
            else -> effectiveCycleClosed(phase, offseasonWeekIndex, ledger.cycleClosed)
        }
}

enum class RecruitingHubTab {
    Recommended,
    AllHsJuco,
    HighSchool,
    JuniorCollege,
    TransferPortal,
    BoardTargets,
    Committed,
    Lost,
}

object RecruitingHubDefaults {
  // Spec tab order ([planning/recruiting-engine.md#tabs], [MEGA-RECR-037]).
    val tabs: List<RecruitingHubTab> = listOf(
        RecruitingHubTab.Recommended,
        RecruitingHubTab.AllHsJuco,
        RecruitingHubTab.HighSchool,
        RecruitingHubTab.JuniorCollege,
        RecruitingHubTab.TransferPortal,
        RecruitingHubTab.BoardTargets,
        RecruitingHubTab.Committed,
        RecruitingHubTab.Lost,
    )

    val defaultTab: RecruitingHubTab = RecruitingHubTab.Recommended
}
