package dev.novux.dynasty.cbb.core.advance

import org.junit.Assert.assertTrue
import org.junit.Test

class OffseasonPortalSimWeekConfirmationTest {
    @Test
    fun confirmation_includesPortalNudgeAndSpendLines() {
        val confirmation = SimWeekAdvanceResolver.buildConfirmation(
            SimWeekAdvanceInput(
                nextWeekLabel = "Offseason · Week 4",
                unplayedUserGameCount = 0,
                recruitingPointsAvailable = 800,
                userManualRecruitingSpent = 0,
                recruitingHubOpen = true,
                estimatedAutoRecruitingSpend = 150,
                portalRecruitingWeekOpen = true,
                unresolvedOptionalTaskCount = 2,
            ),
        )
        assertTrue(confirmation.needsDialog)
        assertTrue(
            confirmation.lines.any { it.kind == SimWeekConfirmationLineKind.OffseasonPortalOptionalNudges },
        )
        assertTrue(
            confirmation.lines.any { it.kind == SimWeekConfirmationLineKind.PortalUserSchoolAutoSpend },
        )
        assertTrue(
            confirmation.lines.none { it.kind == SimWeekConfirmationLineKind.UserRecruitingAutoMaintain },
        )
    }
}
