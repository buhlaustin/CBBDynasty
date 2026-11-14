package dev.novux.dynasty.cbb.core.calendar

/** Calendar tab density follows week index + scheduled games ([MEGA-GDD-075]). */
object UserCalendarScheduleDensityPolicy {
    const val MEGA_ID: String = "MEGA-GDD-075"

    fun gamesScheduledInWeek(row: UserCalendarWeekRow): Int = row.games.size

    fun currentWeekRow(rows: List<UserCalendarWeekRow>): UserCalendarWeekRow? =
        rows.firstOrNull { it.isCurrentWeek }
}