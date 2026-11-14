package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitingHubDefaultsTest {
    @Test
    fun defaultTab_isRecommended() {
        assertEquals(RecruitingHubTab.Recommended, RecruitingHubDefaults.defaultTab)
        assertEquals(RecruitingHubTab.Recommended, RecruitingHubDefaults.tabs.first())
    }

    @Test
    fun hubAccessible_yearLong_inRegularSeason() {
        assertTrue(RecruitingCyclePolicy.hubAccessible(CalendarPhase.RegularSeason))
    }

    @Test
    fun hubAccessible_offseasonFromWeekOne() {
        assertTrue(RecruitingCyclePolicy.hubAccessible(CalendarPhase.Offseason, offseasonWeekIndex = 1))
        assertTrue(RecruitingCyclePolicy.hubAccessible(CalendarPhase.Offseason, offseasonWeekIndex = 2))
    }

    @Test
    fun hubNavigable_blocksWhenCycleClosed() {
        assertEquals(
            false,
            RecruitingCyclePolicy.hubNavigable(
                CalendarPhase.Offseason,
                offseasonWeekIndex = 7,
                ledgerCycleClosed = false,
            ),
        )
        assertEquals(
            false,
            RecruitingCyclePolicy.hubNavigable(
                CalendarPhase.Offseason,
                offseasonWeekIndex = 7,
                ledgerCycleClosed = true,
            ),
        )
        assertTrue(
            RecruitingCyclePolicy.hubNavigable(
                CalendarPhase.Offseason,
                offseasonWeekIndex = 6,
                ledgerCycleClosed = false,
            ),
        )
        assertEquals(
            false,
            RecruitingCyclePolicy.hubNavigable(
                CalendarPhase.Preseason,
                offseasonWeekIndex = null,
                ledgerCycleClosed = true,
            ),
        )
    }

    @Test
    fun recruitingMenuTreatsCycleClosed_preseasonUntilLedgerLoads() {
        assertTrue(
            RecruitingCyclePolicy.recruitingMenuTreatsCycleClosed(
                CalendarPhase.Preseason,
                offseasonWeekIndex = null,
                ledger = null,
            ),
        )
        assertTrue(
            RecruitingCyclePolicy.recruitingMenuTreatsCycleClosed(
                CalendarPhase.Preseason,
                offseasonWeekIndex = null,
                ledger = RecruitingCycleLedger(
                    cycleSeasonIndex = 1,
                    programPrestige = 50,
                    weeklyPoolBase = 100,
                    pointsAvailable = 0,
                    cycleClosed = true,
                ),
            ),
        )
    }

    @Test
    fun effectiveCycleClosed_administrationWeeksEvenWhenLedgerOpen() {
        assertTrue(
            RecruitingCyclePolicy.effectiveCycleClosed(
                CalendarPhase.Offseason,
                offseasonWeekIndex = 8,
                ledgerCycleClosed = false,
            ),
        )
        assertEquals(
            false,
            RecruitingCyclePolicy.effectiveCycleClosed(
                CalendarPhase.Offseason,
                offseasonWeekIndex = 6,
                ledgerCycleClosed = false,
            ),
        )
    }
}
