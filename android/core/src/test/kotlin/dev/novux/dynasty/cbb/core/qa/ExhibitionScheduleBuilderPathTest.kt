package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.schedule.ExhibitionScheduleUxPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-075] */
class ExhibitionScheduleBuilderPathTest {
    @Test
    fun exhibitionBuilder_wiredInPanelAndRepository() {
        val root = repoRoot()
        val panel = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/ExhibitionSchedulePanel.kt",
        ).readText()
        val repo = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/preseason/PreseasonScheduleRepository.kt",
        ).readText()
        assertEquals("MEGA-FRAME-075", ExhibitionScheduleUxPolicy.MEGA_FRAME_ID)
        assertTrue(panel.contains("ExhibitionOpponentPickerSheet"))
        assertTrue(panel.contains("ExhibitionSlotCard"))
        assertTrue(repo.contains("loadNonConferenceOpponents"))
        assertTrue(repo.contains("exhibitionStubs"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
