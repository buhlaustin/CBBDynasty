package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-026], [MEGA-FRAME-098], [MEGA-FRAME-099] */
class PreseasonTaskDeepLinkPathTest {
    @Test
    fun coreAndPreseasonUiWirePerTaskDeepLinks() {
        val root = repoRoot()
        val tasks = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/dashboard/PreseasonRequiredDashboardTasks.kt",
        ).readText()
        val deepLinks = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/dashboard/PreseasonTaskDeepLinks.kt",
        ).readText()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonScreen.kt",
        ).readText()
        val chips = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonRequiredTaskChips.kt",
        ).readText()
        assertTrue(tasks.contains("PreseasonTaskDeepLinks.deepLink"))
        assertTrue(tasks.contains("preseasonTaskId = taskId"))
        assertTrue(deepLinks.contains("PreseasonTaskIncomingClassSeed"))
        assertTrue(screen.contains("PreseasonRequiredTaskChips"))
        assertTrue(screen.contains("navigatePreseasonDashboardTask"))
        assertTrue(screen.contains("animateScrollToItem"))
        assertTrue(chips.contains("onOpenTask"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
