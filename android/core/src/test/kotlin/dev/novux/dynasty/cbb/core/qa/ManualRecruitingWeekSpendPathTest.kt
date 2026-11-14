package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-110] */
class ManualRecruitingWeekSpendPathTest {
    @Test
    fun saveSlot_wiresManualRecruitingSpendOnConfirmAndAdvance() {
        val root = repoRoot()
        val repo = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/save/SaveSlotRepository.kt",
        ).readText()
        val economy = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/recruiting/RecruitingEconomyRepository.kt",
        ).readText()
        assertTrue(repo.contains("recruitingManualWeekSpendRepository"))
        assertTrue(repo.contains("totalForDynasty"))
        assertTrue(economy.contains("manualWeekSpendRepository.recordSpend"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}