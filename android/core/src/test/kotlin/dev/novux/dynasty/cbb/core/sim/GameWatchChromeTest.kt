package dev.novux.dynasty.cbb.core.sim

import org.junit.Assert.assertEquals
import org.junit.Test

class GameWatchChromeTest {
    @Test
    fun periodLabelsFollowGameClock() {
        val first = GameWatchChromeFactory.create(
            gameClockSeconds = GameSimClock.HALF_SECONDS + 30,
            shotClockSeconds = 24,
            inOvertime = false,
            overtimePeriods = 0,
        )
        assertEquals(GameWatchPeriod.FirstHalf, first.period)
        assertEquals("20:30", first.gameClockDisplay)
        val second = GameWatchChromeFactory.create(
            gameClockSeconds = GameSimClock.HALF_SECONDS - 1,
            shotClockSeconds = 18,
            inOvertime = false,
            overtimePeriods = 0,
        )
        assertEquals(GameWatchPeriod.SecondHalf, second.period)
        val ot = GameWatchChromeFactory.create(
            gameClockSeconds = 120,
            shotClockSeconds = 30,
            inOvertime = true,
            overtimePeriods = 2,
        )
        assertEquals(GameWatchPeriod.Overtime, ot.period)
        assertEquals(2, ot.overtimeNumber)
        assertEquals(30, ot.shotClockSeconds)
    }
}
