package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.player.draft.PlayerDraftProjectionPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerDraftProjectionPathTest {
    @Test
    fun draftProjectionOnPlayerOverviewContentAndPreviewSheet() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val sheet = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/player/PlayerCardSheet.kt")
            .readText()
        val line = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/player/PlayerDraftProjectionLine.kt")
            .readText()
        assertTrue(sheet.contains(PlayerDraftProjectionPolicy.OVERVIEW_COMPOSABLE))
        assertTrue(line.contains(PlayerDraftProjectionPolicy.ENGINE))
        assertTrue(line.contains("nationalTop60RankByPlayerId"))
        assertTrue(line.contains("PlayerDraftProjectionPolicy.NATIONAL_POOL"))
        val shell = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt")
            .readText()
        assertTrue(shell.contains(PlayerDraftProjectionPolicy.NATIONAL_POOL))
        assertTrue(shell.contains("nationalTop60RankByPlayerId"))
    }
}
