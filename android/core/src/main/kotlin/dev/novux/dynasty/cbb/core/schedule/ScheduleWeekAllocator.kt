package dev.novux.dynasty.cbb.core.schedule

import dev.novux.dynasty.cbb.core.preseason.tournament.RegularSeasonSlotBudget

/** One RS game per calendar week for a single team ([MEGA-NOTES-047]). */
object ScheduleWeekAllocator {
    fun allocate(
        count: Int,
        weekRange: IntRange,
        alreadyTaken: Set<Int> = emptySet(),
    ): List<Int> {
        if (count <= 0) return emptyList()
        val taken = alreadyTaken.toMutableSet()
        val weeks = mutableListOf<Int>()
        var cursor = weekRange.first
        while (weeks.size < count && cursor <= weekRange.last) {
            if (cursor !in taken) {
                weeks += cursor
                taken += cursor
            }
            cursor++
        }
        if (weeks.size < count) {
            for (candidate in 1..RegularSeasonSlotBudget.RS_CAP) {
                if (candidate !in taken) {
                    weeks += candidate
                    taken += candidate
                }
                if (weeks.size >= count) break
            }
        }
        return weeks
    }
}
