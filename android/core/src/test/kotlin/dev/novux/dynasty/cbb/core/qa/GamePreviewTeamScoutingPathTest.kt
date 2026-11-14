package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-041] */
class GamePreviewTeamScoutingPathTest {
    @Test
    fun gamePreview_teamScoutingAndDynastyH2hWired() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preview/GamePreviewScreen.kt",
        ).readText()
        val shell = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt",
        ).readText()
        assertTrue(screen.contains("game_preview_team_heading"))
        assertTrue(screen.contains("GamePreviewScoutingTable"))
        assertTrue(shell.contains("HeadToHeadHistoryResolver.collectDynastyMeetings"))
        assertTrue(shell.contains("GamePreviewScoutingBuilder.fromOverview"))
        assertTrue(!shell.contains("HeadToHeadHistory.summarize(emptyList())"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
