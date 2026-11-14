package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-083] */
class SimWeekLeagueBatchConfirmPathTest {
    @Test
    fun advanceConfirmation_surfacesLeagueWeekBatchDuringRegularSeason() {
        val root = repoRoot()
        val repo = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/save/SaveSlotRepository.kt",
        ).readText()
        val dialog = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/advance/SimWeekAdvanceDialog.kt",
        ).readText()
        assertTrue(repo.contains("simulatesLeagueWeekBatch"))
        assertTrue(repo.contains("CalendarPhase.RegularSeason"))
        assertTrue(dialog.contains("SimWeekConfirmationLineKind.LeagueWeekBatch"))
        assertTrue(dialog.contains("advance_confirm_line_league_batch"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}