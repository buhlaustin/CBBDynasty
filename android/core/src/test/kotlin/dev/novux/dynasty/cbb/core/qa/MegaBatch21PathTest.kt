package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import dev.novux.dynasty.cbb.core.notification.NotificationEventKind
import dev.novux.dynasty.cbb.core.notification.NotificationFeedCarouselRowPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedComposer
import dev.novux.dynasty.cbb.core.notification.NotificationFeedDisciplineRowPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedFundraisingRowPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedMedicalRedshirtRowPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedPlayWeekDeepLinkPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedSelectionSundayRowPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationSnapshotInput
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MegaBatch21PathTest {
    @Test
    fun notificationFeedCatalogBatchWired() {
        assertEquals("MEGA-SHELL-049", NotificationFeedDisciplineRowPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-050", NotificationFeedCarouselRowPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-051", NotificationFeedSelectionSundayRowPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-052", NotificationFeedMedicalRedshirtRowPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-053", NotificationFeedFundraisingRowPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-055", NotificationFeedPlayWeekDeepLinkPolicy.MEGA_ID)
        assertEquals(
            DashboardDeepLink.MarchMayhemHub,
            NotificationFeedSelectionSundayRowPolicy.deepLink,
        )
        assertTrue(NotificationFeedPlayWeekDeepLinkPolicy.routesToGamePreview(DashboardDeepLink.PlayWeekGame))

        val feed = NotificationFeedComposer.compose(
            NotificationSnapshotInput(
                asOfMs = 1L,
                disciplineHeadline = "Conduct — Player",
                carouselApprovalBodies = listOf("Extension offer"),
                selectionSundayBody = "Seeded",
                medicalRedshirtBodies = listOf("Player may qualify"),
                fundraisingCompleteBodies = listOf("Gala completed"),
                gameThisWeekLabel = "Week 3 — vs Opponent",
            ),
        )
        val kinds = feed.map { it.kind }.toSet()
        assertTrue(NotificationEventKind.DisciplinePending in kinds)
        assertTrue(NotificationEventKind.CarouselApproval in kinds)
        assertTrue(NotificationEventKind.SelectionSunday in kinds)
        assertTrue(NotificationEventKind.MedicalRedshirtEligible in kinds)
        assertTrue(NotificationEventKind.FundraisingComplete in kinds)
        assertEquals(
            DashboardDeepLink.PlayWeekGame,
            feed.first { it.kind == NotificationEventKind.GameThisWeek }.deepLink,
        )

        val panel = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dashboard/DashboardNotificationFeedPanel.kt",
        ).readText()
        assertTrue(panel.contains("NotificationFeedPlayWeekDeepLinkPolicy"))
        assertTrue(panel.contains("onGamePreviewOpen"))
        val shell = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt",
        ).readText()
        assertTrue(shell.contains("DashboardNotificationSnapshotSidecarLoader"))
        assertTrue(shell.contains("ShellWeekGamePreviewLauncher"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}