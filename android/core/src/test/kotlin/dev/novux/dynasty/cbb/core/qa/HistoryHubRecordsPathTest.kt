package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryHubRecordsPathTest {
    @Test
    fun historyHubShowsRecordBookTab() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/history/HistoryHubScreen.kt",
        ).readText()
        assertTrue(screen.contains("HistoryHubSurfacePolicy"))
        assertTrue(screen.contains("history_tab_records"))
    }
}