package dev.novux.dynasty.cbb.core.sim

/**
 * Live watch header — period + game clock ([MEGA-SIM-122]) and shot-clock tension ([MEGA-SIM-123]).
 */
enum class GameWatchPeriod {
    FirstHalf,
    SecondHalf,
    Overtime,
}

data class GameWatchChrome(
    val period: GameWatchPeriod,
    val overtimeNumber: Int = 0,
    val gameClockDisplay: String,
    val shotClockSeconds: Int,
)

object GameWatchChromeFactory {
    fun create(
        gameClockSeconds: Int,
        shotClockSeconds: Int,
        inOvertime: Boolean,
        overtimePeriods: Int,
    ): GameWatchChrome {
        val period = when {
            inOvertime && overtimePeriods > 0 -> GameWatchPeriod.Overtime
            gameClockSeconds > GameSimClock.HALF_SECONDS -> GameWatchPeriod.FirstHalf
            else -> GameWatchPeriod.SecondHalf
        }
        return GameWatchChrome(
            period = period,
            overtimeNumber = overtimePeriods,
            gameClockDisplay = formatGameClock(gameClockSeconds),
            shotClockSeconds = shotClockSeconds.coerceAtLeast(0),
        )
    }

    fun formatGameClock(seconds: Int): String {
        val clamped = maxOf(0, seconds)
        val minutes = clamped / 60
        val secs = clamped % 60
        return "%d:%02d".format(minutes, secs)
    }
}
