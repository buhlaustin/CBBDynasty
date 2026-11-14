package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.seasonreview.SeasonReviewNextCyclePreviewPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonReviewNextCyclePreviewPathTest {
    @Test
    fun recapShowsNextCycleNilAndRecruitingPreview() {
        assertEquals("MEGA-GDD-135", SeasonReviewNextCyclePreviewPolicy.MEGA_ID)
        val root = repoRoot()
        val content = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/seasonreview/SeasonReviewContent.kt",
        ).readText()
        val factory = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/seasonreview/SeasonReviewSnapshotFactory.kt",
        ).readText()
        assertTrue(content.contains("season_review_nil_next_cycle"))
        assertTrue(content.contains("season_review_next_cycle_hint"))
        assertTrue(factory.contains("nextCycleNilPoolUsd"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}