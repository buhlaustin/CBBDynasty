package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.history.HistoryHubGoatRecordPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryHubGoatRecordPathTest {
    @Test
    fun historyHubTabsAndOnlineHint() {
        assertEquals("MEGA-GDD-136", HistoryHubGoatRecordPolicy.MEGA_ID)
        val screen = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/history/HistoryHubScreen.kt",
        ).readText()
        assertTrue(screen.contains("history_tab_goat"))
        assertTrue(screen.contains("HistoryHubSurfacePolicy"))
        assertTrue(screen.contains("history_hub_online_hint"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}