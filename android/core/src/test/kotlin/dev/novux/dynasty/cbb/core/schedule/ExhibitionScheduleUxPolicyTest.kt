package dev.novux.dynasty.cbb.core.schedule

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExhibitionScheduleUxPolicyTest {
    @Test
    fun zeroExhibitions_valid() {
        assertNull(ExhibitionScheduleUxPolicy.draftIssue(0, emptyList()))
        assertTrue(ExhibitionScheduleUxPolicy.validateDraft(0, emptyList()))
    }

    @Test
    fun missingOpponent_surfacesIssue() {
        val issue = ExhibitionScheduleUxPolicy.draftIssue(
            1,
            listOf(ExhibitionGameDraft()),
        )
        assertEquals(ExhibitionDraftIssue.MissingOpponent, issue)
    }
}