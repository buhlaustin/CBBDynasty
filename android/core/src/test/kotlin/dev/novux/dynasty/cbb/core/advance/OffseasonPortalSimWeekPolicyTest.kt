package dev.novux.dynasty.cbb.core.advance

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OffseasonPortalSimWeekPolicyTest {
    @Test
    fun portalNudgeLine_whenUnresolvedDuringPortalWeek() {
        val input = SimWeekAdvanceInput(
            nextWeekLabel = "Offseason · Week 3",
            unplayedUserGameCount = 0,
            recruitingPointsAvailable = 0,
            userManualRecruitingSpent = 0,
            recruitingHubOpen = false,
            estimatedAutoRecruitingSpend = 0,
            portalRecruitingWeekOpen = true,
            unresolvedOptionalTaskCount = 2,
        )
        assertTrue(OffseasonPortalSimWeekPolicy.showPortalOptionalNudgeLine(input))
    }

    @Test
    fun portalSpendLine_replacesGeneric_whenNudgesRemain() {
        val input = SimWeekAdvanceInput(
            nextWeekLabel = "Offseason · Week 3",
            unplayedUserGameCount = 0,
            recruitingPointsAvailable = 500,
            userManualRecruitingSpent = 0,
            recruitingHubOpen = true,
            estimatedAutoRecruitingSpend = 120,
            portalRecruitingWeekOpen = true,
            unresolvedOptionalTaskCount = 1,
        )
        assertTrue(OffseasonPortalSimWeekPolicy.usePortalSpendLineInsteadOfGenericAutoMaintain(input))
    }

    @Test
    fun portalSpendLine_absentWhenNoUnresolvedNudges() {
        val input = SimWeekAdvanceInput(
            nextWeekLabel = "Offseason · Week 3",
            unplayedUserGameCount = 0,
            recruitingPointsAvailable = 500,
            userManualRecruitingSpent = 0,
            recruitingHubOpen = true,
            estimatedAutoRecruitingSpend = 120,
            portalRecruitingWeekOpen = true,
            unresolvedOptionalTaskCount = 0,
        )
        assertFalse(OffseasonPortalSimWeekPolicy.usePortalSpendLineInsteadOfGenericAutoMaintain(input))
    }
}
