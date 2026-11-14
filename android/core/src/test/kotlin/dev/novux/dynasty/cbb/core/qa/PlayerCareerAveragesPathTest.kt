package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerCareerAveragesPathTest {
    @Test
    fun careerAveragesCardOnStatsTab() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val ui = root.resolve(PlayerCareerAveragesPath.UI).readText()
        assertTrue(ui.contains(PlayerCareerAveragesPath.MEGA_ID))
        assertTrue(ui.contains("PlayerCareerAveragesCard"))
        assertTrue(ui.contains(PlayerCareerAveragesPath.FOUR_CARDS))
        assertTrue(ui.contains("careerAverageLines"))
        assertTrue(ui.contains("player_stat_fg_pct"))
    }
}
