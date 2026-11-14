package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerCareerTotalsPathTest {
    @Test
    fun careerTotalsCardOnStatsTab() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val ui = root.resolve(PlayerCareerTotalsPath.UI).readText()
        assertTrue(ui.contains(PlayerCareerTotalsPath.MEGA_ID))
        assertTrue(ui.contains("PlayerCareerTotalsCard"))
        assertTrue(ui.contains("PlayerStatsFourCards"))
        assertTrue(ui.contains("summary.careerTotals"))
    }
}
