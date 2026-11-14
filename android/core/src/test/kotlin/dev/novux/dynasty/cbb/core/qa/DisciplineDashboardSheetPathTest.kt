package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-032], [MEGA-QA-093] */
class DisciplineDashboardSheetPathTest {
    @Test
    fun inSeasonShell_wiresDisciplineRequiredCardAndSheet() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt",
        ).readText()
        val sheet = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/discipline/DisciplineDecisionBottomSheet.kt",
        ).readText()
        assertTrue(sheet.contains(DisciplineDashboardFirstPaint.FRAME_CROSS_REF))
        assertTrue(sheet.contains("DisciplineDecisionBottomSheet"))
        assertTrue(screen.contains("DisciplineDecisionBottomSheet"))
        assertTrue(screen.contains("DisciplineDashboardFirstPaint.SHELL_DEDICATED_EFFECT_MARKER"))
        assertTrue(screen.contains(DisciplineDashboardFirstPaint.SHELL_HYDRATION_EFFECT_MARKER))
        assertTrue(screen.contains("DashboardRequiredTasksPanel"))
        assertTrue(screen.contains("disciplineSheetTarget"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
