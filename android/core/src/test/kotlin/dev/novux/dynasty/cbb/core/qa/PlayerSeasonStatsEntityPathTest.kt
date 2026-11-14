package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.player.stats.PlayerSeasonStatsPersistencePolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerSeasonStatsEntityPathTest {
    @Test
    fun playerSeasonStatsEntityBacksStatsAndGamesTabs() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val entity = root.resolve(PlayerSeasonStatsEntityPath.ENTITY).readText()
        assertTrue(entity.contains(PlayerSeasonStatsEntityPath.MEGA_ID))
        assertTrue(entity.contains(PlayerSeasonStatsPersistencePolicy.ENTITY))
        assertTrue(entity.contains(PlayerSeasonStatsPersistencePolicy.TABLE))
        val repo = root.resolve(PlayerSeasonStatsEntityPath.REPO).readText()
        assertTrue(repo.contains("playerSeasonStatsDao"))
        assertTrue(repo.contains("PlayerSeasonStatsEntity"))
    }
}
