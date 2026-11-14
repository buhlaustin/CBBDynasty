package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.preseason.IncomingClassSeedRunProgress
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-071] */
class IncomingClassSeedPanelProgressPathTest {
    @Test
    fun panelShowsDeterminateSeedProgress() {
        val root = repoRoot()
        val panel = root.resolve(IncomingClassSeedPanelIoPath.PANEL_SOURCE).readText()
        val repository = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/preseason/IncomingClassSeedRepository.kt",
        ).readText()
        assertEquals("MEGA-FRAME-071", IncomingClassSeedRunProgress.MEGA_FRAME_ID)
        assertTrue(panel.contains("LinearProgressIndicator"))
        assertTrue(panel.contains("IncomingClassSeedRunProgress.fraction"))
        assertTrue(panel.contains("runPhase"))
        assertTrue(repository.contains("onPhase"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
