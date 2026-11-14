package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.signingday.SigningDayProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SigningDayShellDashboardTasksTest {
    @Test
    fun incompleteClass_emitsSingleLauncher() {
        val tasks = SigningDayShellDashboardTasks.build(SigningDayProgress(cycleSeasonIndex = 1))
        assertEquals(1, tasks.size)
        assertEquals(DashboardTaskId.SigningDayShellLauncher, tasks.single().id)
        assertEquals(DashboardDeepLink.SigningDayRankings, tasks.single().deepLink)
    }

    @Test
    fun afterRankings_deepLinksToPositions() {
        val tasks = SigningDayShellDashboardTasks.build(
            SigningDayProgress(
                cycleSeasonIndex = 1,
                rankingsViewed = true,
                positionsConfirmed = false,
            ),
        )
        assertEquals(DashboardDeepLink.SigningDayPositions, tasks.single().deepLink)
    }

    @Test
    fun hardSignComplete_emitsNothing() {
        assertTrue(
            SigningDayShellDashboardTasks.build(
                SigningDayProgress(cycleSeasonIndex = 1, hardSignComplete = true),
            ).isEmpty(),
        )
    }
}