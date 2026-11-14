package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.coach.card.history.CoachCareerSeasonPersistencePolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachCareerSeasonEntityPathTest {
    @Test
    fun coachCareerSeasonEntityPersistedForHistoryTab() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val entity = root.resolve(CoachCareerSeasonEntityPath.ENTITY).readText()
        assertTrue(entity.contains(CoachCareerSeasonEntityPath.MEGA_ID))
        assertTrue(entity.contains(CoachCareerSeasonPersistencePolicy.ENTITY))
        assertTrue(entity.contains(CoachCareerSeasonPersistencePolicy.TABLE))
        val repo = root.resolve(CoachCareerSeasonEntityPath.REPOSITORY).readText()
        assertTrue(repo.contains("archiveCompletedSeason"))
        assertTrue(repo.contains("coachCareerSeasonDao"))
        val deepTabs = root.resolve(CoachCareerSeasonEntityPath.DEEP_TABS_REPO).readText()
        assertTrue(deepTabs.contains("CoachCareerSeasonMergeEngine"))
    }
}
