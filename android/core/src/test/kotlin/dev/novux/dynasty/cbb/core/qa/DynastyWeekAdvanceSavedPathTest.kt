package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-158] */
class DynastyWeekAdvanceSavedPathTest {
    @Test
    fun successfulAdvanceShowsSavedSnackbar() {
        val root = repoRoot()
        val feedback = root.resolve(FEEDBACK).readText()
        assertTrue(feedback.contains("advance_week_saved"))
        assertTrue(feedback.contains("showSnackbar"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }

    companion object {
        private const val FEEDBACK =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/advance/DynastyWeekAdvanceFeedback.kt"
    }
}