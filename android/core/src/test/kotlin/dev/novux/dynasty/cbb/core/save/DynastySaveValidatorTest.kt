package dev.novux.dynasty.cbb.core.save

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DynastySaveValidatorTest {
    private val occupiedSlot = DynastySaveValidator.SlotSnapshot(
        slotIndex = 0,
        dynastyId = "d1",
        packId = DynastySchema.DEFAULT_PACK_ID,
        isEmpty = false,
    )

    private val metadata = DynastySaveValidator.MetadataSnapshot(
        dynastyId = "d1",
        packId = DynastySchema.DEFAULT_PACK_ID,
        seasonIndex = 1,
        phase = CalendarPhase.Offseason,
    )

    @Test
    fun ok_whenMetadataMatchesSlot() {
        assertNull(DynastySaveValidator.validateBeforeLoad(occupiedSlot, metadata))
    }

    @Test
    fun fails_whenMetadataMissing() {
        val failure = DynastySaveValidator.validateBeforeLoad(occupiedSlot, null)
        assertEquals(DynastyLoadFailure.MissingMetadata("d1"), failure)
    }

    @Test
    fun fails_whenPackMismatch() {
        val badSlot = occupiedSlot.copy(packId = "other-pack")
        val failure = DynastySaveValidator.validateBeforeLoad(badSlot, metadata)
        assertEquals(
            DynastyLoadFailure.PackIdMismatch("other-pack", DynastySchema.DEFAULT_PACK_ID),
            failure,
        )
    }

    @Test
    fun signedDynasty_allowsPlaceholderWithoutCoachOrRoster() {
        assertNull(DynastySaveValidator.validateSignedDynastyCore(hasCoach = false, rosterPlayerCount = 0))
    }

    @Test
    fun signedDynasty_rejectsCoachWithoutRoster() {
        val failure = DynastySaveValidator.validateSignedDynastyCore(hasCoach = true, rosterPlayerCount = 0)
        assertEquals(true, failure is DynastyLoadFailure.IncompleteNewGameSign)
    }

    @Test
    fun signedDynasty_rejectsPartialRoster() {
        val failure = DynastySaveValidator.validateSignedDynastyCore(hasCoach = true, rosterPlayerCount = 7)
        assertEquals(true, failure is DynastyLoadFailure.IncompleteNewGameSign)
    }

    @Test
    fun calendar_rejectsOutOfRangeWeek() {
        val failure = DynastySaveValidator.validateCalendar(
            DynastySaveValidator.CalendarSnapshot(
                seasonIndex = 1,
                phase = CalendarPhase.Exhibition,
                activeWeekIndex = 99,
                committedSeasonIndex = null,
                committedPhase = null,
                committedWeekIndex = null,
            ),
        )
        assertEquals(true, failure is DynastyLoadFailure.InvalidCalendar)
    }
}
