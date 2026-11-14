package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachCardDeepTabsPathTest {
    @Test
    fun deepTabsEngineRepositoryAndUiExist() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(CoachCardDeepTabsPath.ENGINE).readText().contains(CoachCardDeepTabsPath.MEGA_ID))
        assertTrue(root.resolve(CoachCardDeepTabsPath.UI).readText().contains("CoachSeasonHistoryList"))
        assertTrue(root.resolve(CoachCardDeepTabsPath.REPOSITORY).readText().contains("loadHeadCoachDeepTabs"))
        assertTrue(root.resolve(CoachCardDeepTabsPath.HEAD_SCREEN).readText().contains("CoachCardHistoryDeepTab"))
    }
}
