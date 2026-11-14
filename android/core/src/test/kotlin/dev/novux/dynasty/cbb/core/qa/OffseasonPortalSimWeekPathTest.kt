package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.advance.OffseasonPortalSimWeekPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-068] */
class OffseasonPortalSimWeekPathTest {
    @Test
    fun megaFrame068_wiresPolicyResolverAggregatorAndOffseasonScreen() {
        val root = repoRoot()
        val policy = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/advance/OffseasonPortalSimWeekPolicy.kt",
        ).readText()
        val resolver = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/advance/SimWeekAdvanceResolver.kt",
        ).readText()
        val aggregator = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/dashboard/OffseasonAdvanceOptionalTaskAggregator.kt",
        ).readText()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
        ).readText()
        assertEquals(OffseasonPortalSimWeekPolicy.MEGA_FRAME_ID, "MEGA-FRAME-068")
        assertTrue(policy.contains("MEGA-FRAME-068"))
        assertTrue(resolver.contains("OffseasonPortalSimWeekPolicy"))
        assertTrue(aggregator.contains("MEGA-FRAME-068"))
        assertTrue(screen.contains("OffseasonAdvanceOptionalTaskAggregator"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
