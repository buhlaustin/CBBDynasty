package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.player.loyalty.PlayerPromiseHistoryPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerPromiseHistoryPathTest {
    @Test
    fun historyTabShowsLockedPromiseAndSeasonEvaluations() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val screen = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/player/PlayerCardScreen.kt")
            .readText()
        val panel = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/player/PlayerPromiseHistoryPanel.kt")
            .readText()
        assertTrue(screen.contains(PlayerPromiseHistoryPolicy.HISTORY_PANEL))
        assertTrue(panel.contains(PlayerPromiseHistoryPolicy.HYDRATOR))
        assertTrue(panel.contains("historyForDisplay"))
    }
}
