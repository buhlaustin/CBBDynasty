package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

/** In-season sim week should confirm destination even with no optional nudges ([MEGA-FRAME-115]). */
object InSeasonAdvanceConfirmPolicy {
    fun requiresDestinationAcknowledgment(phase: CalendarPhase): Boolean =
        phase == CalendarPhase.RegularSeason ||
            phase == CalendarPhase.Exhibition ||
            phase == CalendarPhase.Postseason
}