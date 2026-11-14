package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class SigningDayShellDashboardPathTest {
    @Test
    fun offseasonWeekSixUsesSingleShellLauncher() {
        val root = repoRoot()
        val aggregator = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/dashboard/OffseasonRequiredDashboardTasks.kt",
        ).readText()
        assertTrue(aggregator.contains("SigningDayShellDashboardTasks"))
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
            ).readText().contains("offseason_signing_day_hint"),
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}