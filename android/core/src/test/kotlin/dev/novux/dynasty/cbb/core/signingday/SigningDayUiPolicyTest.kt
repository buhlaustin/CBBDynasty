package dev.novux.dynasty.cbb.core.signingday

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SigningDayUiPolicyTest {
    @Test
    fun rankingsTab_isRecruitNationalNotTeamPolls() {
        assertTrue(SigningDayUiPolicy.RANKINGS_TAB_IS_RECRUIT_NATIONAL)
        assertEquals(0, SigningDayUiPolicy.DEFAULT_TAB_INDEX)
        assertEquals(60, SigningDayUiPolicy.RANKINGS_LIST_CAP)
    }
}
