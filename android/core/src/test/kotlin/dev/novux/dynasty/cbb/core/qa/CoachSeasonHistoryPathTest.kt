package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachSeasonHistoryPathTest {
    @Test
    fun coachSeasonHistoryListAndExpandDetailExist() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val ui = root.resolve(CoachSeasonHistoryPath.UI).readText()
        assertTrue(ui.contains(CoachSeasonHistoryPath.LIST_COMPOSABLE))
        assertTrue(ui.contains("expandedSeason"))
        assertTrue(root.resolve(CoachSeasonHistoryPath.MODEL).readText().contains("CoachSeasonHistoryDetail"))
        assertTrue(root.resolve(CoachSeasonHistoryPath.RESOLVER).readText().contains("conferenceRecordLabel"))
    }
}
