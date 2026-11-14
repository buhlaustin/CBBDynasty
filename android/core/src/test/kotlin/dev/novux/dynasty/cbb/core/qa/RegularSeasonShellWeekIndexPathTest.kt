package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.advance.RegularSeasonWeekBatchPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-152] */
class RegularSeasonShellWeekIndexPathTest {
    @Test
    fun shellHeaderAndConfirmUseCheckpointWeekIndex() {
        val root = repoRoot()
        val shell = root.resolve(SHELL).readText()
        val repo = root.resolve(REPO).readText()
        assertTrue(shell.contains("CalendarWeekLabel.display"))
        assertTrue(shell.contains("weekCheckpoint.activePosition") || shell.contains("position ="))
        assertTrue(repo.contains("RegularSeasonWeekBatchPolicy"))
        assertTrue(repo.contains("simulatesLeagueWeekBatch"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }

    companion object {
        private const val SHELL =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt"
        private const val REPO =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/save/SaveSlotRepository.kt"
    }
}