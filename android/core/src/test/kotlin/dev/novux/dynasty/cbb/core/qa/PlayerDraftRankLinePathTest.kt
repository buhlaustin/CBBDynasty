package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.player.draft.PlayerDraftRankLinePolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerDraftRankLinePathTest {
    @Test
    fun draftRankOnDynastyPlayerAndCardLine() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val player = root.resolve(PlayerDraftRankLinePath.PLAYER).readText()
        assertTrue(player.contains(PlayerDraftRankLinePolicy.PLAYER_FIELD))
        assertTrue(player.contains(PlayerDraftRankLinePath.MEGA_ID))
        val ui = root.resolve(PlayerDraftRankLinePath.UI).readText()
        assertTrue(ui.contains(PlayerDraftRankLinePath.MEGA_ID))
        assertTrue(ui.contains("player_card_draft_line"))
        assertTrue(root.resolve(PlayerDraftRankLinePath.SHELL).readText().contains("withNationalTop60DraftRank"))
    }
}
