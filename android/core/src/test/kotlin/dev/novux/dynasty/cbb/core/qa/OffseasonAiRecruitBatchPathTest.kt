package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.recruiting.ai.OffseasonAiRecruitBatchPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OffseasonAiRecruitBatchPathTest {
    @Test
    fun advanceRunsAiBatchAndOffseasonHints() {
        assertEquals("MEGA-GDD-118", OffseasonAiRecruitBatchPolicy.MEGA_ID)
        val root = repoRoot()
        val save = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/save/SaveSlotRepository.kt",
        ).readText()
        val offseason = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
        ).readText()
        assertTrue(save.contains("runLeagueWeekIfActive"))
        assertTrue(offseason.contains("offseason_ai_recruit_batch_hint"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}