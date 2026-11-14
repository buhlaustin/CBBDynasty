package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachOverviewAchievementsPathTest {
    @Test
    fun achievementsEngineRepositoryAndOverviewCardExist() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(CoachOverviewAchievementsPath.ENGINE).readText().contains("heritageTitles"))
        assertTrue(root.resolve(CoachOverviewAchievementsPath.UI).readText().contains("CoachOverviewAchievementsCard"))
        assertTrue(root.resolve(CoachOverviewAchievementsPath.REPOSITORY).readText().contains("finalizeSeason"))
    }
}
