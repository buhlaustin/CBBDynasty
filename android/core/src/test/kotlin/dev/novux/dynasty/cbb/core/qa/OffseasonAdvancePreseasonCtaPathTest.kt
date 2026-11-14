package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-067] */
class OffseasonAdvancePreseasonCtaPathTest {
    @Test
    fun offseasonScreen_usesAdvancePolicyForPreseasonCta() {
        val screen = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
        ).readText()
        val policy = repoRoot().resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/offseason/weekten/OffseasonWeekAdvancePolicy.kt",
        ).readText()
        assertTrue(screen.contains("OffseasonWeekAdvancePolicy.showsAdvanceToPreseasonCta"))
        assertTrue(screen.contains("offseason_advance_to_preseason"))
        assertTrue(policy.contains("showsAdvanceToPreseasonCta"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
