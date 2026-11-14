package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachOverviewContractCardPathTest {
    @Test
    fun overviewContractCardShowsTermAndPrestige() {
        val root = repoRoot()
        val card = root.resolve(CoachOverviewContractCardPath.SOURCE).readText()
        val screen = root.resolve(CoachOverviewContractCardPath.SCREEN_SOURCE).readText()
        assertTrue(card.contains(CoachOverviewContractCardPath.LENGTH_STRING))
        assertTrue(card.contains(CoachOverviewContractCardPath.PRESTIGE_STRING))
        assertTrue(card.contains("programPrestige"))
        assertTrue(screen.contains(CoachOverviewContractCardPath.COMPOSABLE))
        assertTrue(screen.contains("programPrestige = programPrestige"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
