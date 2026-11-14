package dev.novux.dynasty.cbb.core.advance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InjuryByeAdvanceConfirmPolicyTest {
    @Test
    fun byeWeekUsesDedicatedInjuryLine() {
        val input = byeInjuryInput(injured = 2)
        assertEquals(
            SimWeekConfirmationLineKind.InjuryWeeksTickByeWeek,
            InjuryByeAdvanceConfirmPolicy.injuryTickLineKind(input),
        )
    }

    @Test
    fun gameWeekKeepsGenericInjuryLine() {
        val input = byeInjuryInput(injured = 1).copy(userOnByeWeek = false, unplayedUserGameCount = 1)
        assertEquals(
            SimWeekConfirmationLineKind.InjuryWeeksTick,
            InjuryByeAdvanceConfirmPolicy.injuryTickLineKind(input),
        )
    }

    @Test
    fun appendSimpleAdvanceOnInjuredOnlyBye() {
        assertTrue(InjuryByeAdvanceConfirmPolicy.appendSimpleAdvanceAfterInjuryBye(byeInjuryInput(injured = 1)))
    }

    @Test
    fun noSimpleAdvanceTailWhenRecruitingAutoWouldFire() {
        val input = byeInjuryInput(injured = 1).copy(
            recruitingHubOpen = true,
            recruitingPointsAvailable = 500,
            estimatedAutoRecruitingSpend = 100,
        )
        assertFalse(InjuryByeAdvanceConfirmPolicy.appendSimpleAdvanceAfterInjuryBye(input))
    }

    @Test
    fun megaFrameId() {
        assertEquals("MEGA-FRAME-080", InjuryByeAdvanceConfirmPolicy.MEGA_FRAME_ID)
    }

    private fun byeInjuryInput(injured: Int) = SimWeekAdvanceInput(
        nextWeekLabel = "Regular Season · Week 8",
        unplayedUserGameCount = 0,
        recruitingPointsAvailable = 0,
        userManualRecruitingSpent = 0,
        recruitingHubOpen = false,
        estimatedAutoRecruitingSpend = 0,
        portalRecruitingWeekOpen = false,
        injuredPlayersTicking = injured,
        userOnByeWeek = true,
    )
}
