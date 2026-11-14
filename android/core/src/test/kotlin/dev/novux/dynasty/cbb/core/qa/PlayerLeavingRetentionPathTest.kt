package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.offseason.leaving.PlayerLeavingRetentionPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerLeavingRetentionPathTest {
    @Test
    fun weekOneConvinceShowsPromiseAndLoyaltyTogether() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val summary = root.resolve(PlayerLeavingRetentionPath.SUMMARY).readText()
        assertTrue(summary.contains(PlayerLeavingRetentionPath.MEGA_ID))
        assertTrue(summary.contains("leaving_retention_promise_loyalty"))
        assertTrue(summary.contains("displayLabel()"))
        val screen = root.resolve(PlayerLeavingRetentionPath.SCREEN).readText()
        assertTrue(screen.contains(PlayerLeavingRetentionPolicy.UI_COMPOSABLE))
        assertTrue(screen.contains("convinceStay"))
    }
}
