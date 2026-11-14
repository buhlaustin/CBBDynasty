package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.qa.RecruitingBonusPointsRemainderPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecruitingBonusPointsRemainderPolicyTest {
    @Test
    fun megaRecr140_partialRemainderAccepted() {
        assertEquals(RecruitingBonusPointsRemainderPath.RECR_ID, "MEGA-RECR-140")
        assertEquals(7, RecruitingBonusPointsRemainderPolicy.coerceUserAmount(7, pointsAvailable = 420))
        assertEquals(7, RecruitingBonusPointsRemainderPolicy.parseUserInput("7", pointsAvailable = 420))
    }

    @Test
    fun rejectsAboveBalanceOrNonPositive() {
        assertNull(RecruitingBonusPointsRemainderPolicy.coerceUserAmount(421, 420))
        assertNull(RecruitingBonusPointsRemainderPolicy.coerceUserAmount(0, 420))
        assertNull(RecruitingBonusPointsRemainderPolicy.parseUserInput("abc", 100))
    }
}
