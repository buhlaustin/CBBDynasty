package dev.novux.dynasty.cbb.core.qa

import org.junit.Assert.assertTrue
import org.junit.Test

class PlayUpdateOverPriorPathTest {
    @Test
    fun updateFlow_requiresContinueAfterMigration() {
        val ids = PlayUpdateOverPriorPath.OPEN_TESTING_UPDATE_FLOW.map { it.id }
        assertTrue(ids.contains("migration"))
        assertTrue(ids.contains("continue"))
        assertTrue(ids.contains("play_update"))
    }

    @Test
    fun ciAutomation_listsMigrationAndLoadFixture() {
        val ci = PlayUpdateOverPriorPath.CI_AUTOMATION
        assertTrue(ci.any { it.contains("Migration") })
        assertTrue(ci.contains("SaveLoadAfterUpdateFixtureTest"))
    }
}
