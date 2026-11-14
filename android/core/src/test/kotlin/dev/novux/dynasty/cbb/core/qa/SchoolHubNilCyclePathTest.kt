package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolHubNilCyclePathTest {
    @Test
    fun schoolHubShowsNilCycleSummary() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/school/SchoolHubOverviewCard.kt",
            ).readText().contains("school_hub_nil_cycle_summary"),
        )
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/school/SchoolHubRepository.kt",
            ).readText().contains("SchoolHubNilCycleSummaryPolicy"),
        )
    }
}