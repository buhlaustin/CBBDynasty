package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.GameConstants
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Optional month/day copy for calendar game rows — week index remains authoritative ([planning/time-system.md]).
 */
object CalendarWeekDisplayDate {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

    private val postseasonWeekStarts = listOf(
        "Mar 14",
        "Mar 17",
        "Mar 20",
        "Mar 23",
        "Mar 26",
        "Mar 29",
        "Apr 1",
    )

    fun gameDateLabel(position: CalendarPosition): String? =
        when (position.phase) {
            CalendarPhase.Exhibition -> format(
                position.seasonIndex,
                month = 11,
                startDay = 1,
                weekIndex = position.weekIndex,
                stepDays = 7,
            )
            CalendarPhase.RegularSeason -> format(
                position.seasonIndex,
                month = 11,
                startDay = 22,
                weekIndex = position.weekIndex,
                stepDays = 4,
            )
            CalendarPhase.ConferenceTournament -> {
                val rsEnd = localDate(position.seasonIndex, 11, 22)
                    .plusDays((GameConstants.MAX_REGULAR_SEASON_GAMES - 1) * 4L)
                val date = rsEnd.plusDays((position.weekIndex - 1) * 3L + 7)
                formatter.format(date)
            }
            CalendarPhase.Postseason ->
                postseasonWeekStarts.getOrNull(position.weekIndex - 1)
            else -> null
        }

    private fun format(seasonIndex: Int, month: Int, startDay: Int, weekIndex: Int, stepDays: Int): String {
        val date = localDate(seasonIndex, month, startDay).plusDays(((weekIndex - 1) * stepDays).toLong())
        return formatter.format(date)
    }

    private fun localDate(seasonIndex: Int, month: Int, day: Int): LocalDate {
        val year = 2000 + seasonIndex.coerceAtLeast(1)
        return LocalDate.of(year, month, day)
    }
}
