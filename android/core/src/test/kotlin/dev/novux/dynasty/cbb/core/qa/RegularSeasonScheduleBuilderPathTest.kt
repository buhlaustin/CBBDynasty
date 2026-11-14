package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.schedule.RegularSeasonScheduleBuilderUxPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-076] */
class RegularSeasonScheduleBuilderPathTest {
    @Test
    fun rsBuilder_showsContextReviewAndValidation() {
        val root = repoRoot()
        val panel = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/RegularSeasonSchedulePanel.kt",
        ).readText()
        val repo = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/preseason/PreseasonScheduleRepository.kt",
        ).readText()
        assertEquals("MEGA-FRAME-076", RegularSeasonScheduleBuilderUxPolicy.MEGA_FRAME_ID)
        assertTrue(panel.contains("RegularSeasonScheduleReviewBottomSheet"))
        assertTrue(panel.contains("loadRegularSeasonBuilderContext"))
        assertTrue(panel.contains("RegularSeasonScheduleSolveResult"))
        assertTrue(repo.contains("buildRegularSeasonSchedule"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
