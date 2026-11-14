package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachPerformanceGoalsPathTest {
    @Test
    fun performanceGoalsEngineSurfacedOnOverviewAndContract() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val engine = root.resolve(CoachPerformanceGoalsPath.ENGINE).readText()
        assertTrue(engine.contains(CoachPerformanceGoalsPath.MEGA_ID))
        assertTrue(engine.contains("CoachPerformanceGoalKind.Postseason"))
        assertTrue(root.resolve(CoachPerformanceGoalsPath.OVERVIEW).readText().contains("HeadCoachPerformanceGoalsEngine"))
    }
}
