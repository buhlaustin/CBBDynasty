package dev.novux.dynasty.cbb.core.hubs

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition

/** Hamburger hub visibility ([planning/game-frame.md#top-menu-hamburger--hubs]). */
object DynastyHubMenuPolicy {
    fun conferenceTournamentMenuVisible(position: CalendarPosition): Boolean =
        position.phase == CalendarPhase.ConferenceTournament

    /** March Mayhem unlocks after Selection Sunday field is published, not merely entering postseason phase. */
    fun marchMayhemMenuVisible(
        position: CalendarPosition,
        selectionSundayPublished: Boolean,
    ): Boolean = position.phase == CalendarPhase.Postseason && selectionSundayPublished
}
