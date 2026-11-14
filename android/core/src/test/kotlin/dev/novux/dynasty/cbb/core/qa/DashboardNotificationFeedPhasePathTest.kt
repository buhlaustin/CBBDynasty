package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-037] */
class DashboardNotificationFeedPhasePathTest {
    @Test
    fun offseasonAndPreseason_wiresDashboardNotificationFeed() {
        val root = repoRoot()
        val offseason = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
        ).readText()
        val preseason = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonScreen.kt",
        ).readText()
        val loader = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/notification/DashboardNotificationSnapshotLoader.kt",
        ).readText()
        assertTrue(loader.contains(DashboardNotificationFeedPhasePath.SNAPSHOT_LOADER))
        assertTrue(offseason.contains("DashboardNotificationFeedPanel"))
        assertTrue(offseason.contains("showNotificationFeed"))
        assertTrue(preseason.contains("DashboardNotificationFeedPanel"))
        assertTrue(preseason.contains("showNotificationFeed"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
