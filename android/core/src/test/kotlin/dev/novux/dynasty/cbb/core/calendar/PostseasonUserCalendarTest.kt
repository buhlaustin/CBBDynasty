package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.selection.NcbaBidType
import dev.novux.dynasty.cbb.core.selection.NcbaSelectionEntry
import dev.novux.dynasty.cbb.core.selection.SelectionSundaySnapshot
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostseasonUserCalendarTest {
    @Test
    fun directAtLarge_skipsOpeningRound() {
        val snap = snapshot(
            NcbaSelectionEntry("u", NcbaBidType.DirectAtLarge, 5, 0.0, 10),
        )
        assertTrue(PostseasonUserCalendar.skipsNcbaOpeningRound(snap, "u"))
    }

    @Test
    fun openingRoundBubble_doesNotSkip() {
        val snap = snapshot(
            NcbaSelectionEntry("u", NcbaBidType.OpeningRound, 55, 0.0, 55),
        )
        assertFalse(PostseasonUserCalendar.skipsNcbaOpeningRound(snap, "u"))
    }

    private fun snapshot(vararg entries: NcbaSelectionEntry) =
        SelectionSundaySnapshot(
            seasonIndex = 1,
            ncbaField = entries.toList(),
            nciField = emptyList(),
            heritageField = emptyList(),
        )
}