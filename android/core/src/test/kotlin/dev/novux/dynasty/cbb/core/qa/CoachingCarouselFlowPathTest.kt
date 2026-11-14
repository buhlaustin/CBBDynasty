package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachingCarouselFlowPathTest {
    @Test
    fun carouselFlowUsesPlannerNotStubs() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(CoachingCarouselFlowPath.POLICY).readText().contains(CoachingCarouselFlowPath.MEGA_ID))
        assertTrue(
            root.resolve(CoachingCarouselFlowPath.PLANNER).readText().contains("CarouselUserApprovalPlanner"),
        )
        assertTrue(root.resolve(CoachingCarouselFlowPath.SCREEN).readText().contains(CoachingCarouselFlowPath.MEGA_ID))
    }
}