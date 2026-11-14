package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.signingday.SigningDayConstants

/**
 * Signing Day close → preseason entry aligns calendar with recruiting spec ([planning/recruiting-engine.md], [MEGA-NOTES-065]).
 */
object SigningDayPreseasonRecruitingTransition {
    const val MEGA_ID: String = "MEGA-NOTES-065"

    /** Season 1 chasing opens Offseason Week 2 — not Week 1 ([planning/game-start.md], MEGA-RECR-053). */
    const val SEASON_ONE_CHASING_FIRST_OFFSEASON_WEEK: Int = 2

    fun enrollingPoolSeasonIndex(membershipSeasonIndex: Int): Int = membershipSeasonIndex

    /** Next HS/JUCO pool seeds when entering preseason after signing ([planning/recruiting-engine.md#timeline--seeding]). */
    fun nextEnrollingClassSeasonIndex(membershipSeasonIndex: Int): Int = membershipSeasonIndex + 1

    fun seasonOneOffseasonWeekOne(position: CalendarPosition, membershipSeasonIndex: Int): Boolean =
        membershipSeasonIndex == 1 &&
            position.phase == CalendarPhase.Offseason &&
            position.weekIndex == 1

    /** Wizard lands on OS W1 with pool ready; ledger stays closed until W2. */
    fun seasonOneOffseasonWeekOneLedger(ledger: RecruitingCycleLedger): RecruitingCycleLedger =
        ledger.copy(cycleClosed = true, pointsAvailable = 0)

    fun reopenSeasonOneChasingAtWeekTwo(
        position: CalendarPosition,
        membershipSeasonIndex: Int,
        ledger: RecruitingCycleLedger,
    ): RecruitingCycleLedger? {
        if (membershipSeasonIndex != 1) return null
        if (position.phase != CalendarPhase.Offseason || position.weekIndex != SEASON_ONE_CHASING_FIRST_OFFSEASON_WEEK) {
            return null
        }
        if (!ledger.cycleClosed) return null
        return ledger.copy(cycleClosed = false)
    }

    fun enteringPreseason(position: CalendarPosition): Boolean =
        position.phase == CalendarPhase.Preseason && position.weekIndex == 1

    fun ledgerAfterSigningDayAdministrativeWeeks(
        position: CalendarPosition,
        ledger: RecruitingCycleLedger,
    ): RecruitingCycleLedger {
        val offseasonWeek = position.weekIndex.takeIf { position.phase == CalendarPhase.Offseason }
        if (
            position.phase == CalendarPhase.Offseason &&
            offseasonWeek != null &&
            offseasonWeek > SigningDayConstants.OFFSEASON_WEEK_INDEX
        ) {
            return ledger.copy(cycleClosed = true, pointsAvailable = 0)
        }
        return ledger
    }

    fun ledgerForPreseasonEntry(ledger: RecruitingCycleLedger): RecruitingCycleLedger =
        ledger.copy(cycleClosed = true, pointsAvailable = 0)
}
