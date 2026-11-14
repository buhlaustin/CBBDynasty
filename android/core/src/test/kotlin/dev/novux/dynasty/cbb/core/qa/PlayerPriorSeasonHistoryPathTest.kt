package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerPriorSeasonHistoryPathTest {
    @Test
    fun priorSeasonRowsWithSchoolAndTransfer() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val ui = root.resolve(PlayerPriorSeasonHistoryPath.UI).readText()
        assertTrue(ui.contains(PlayerPriorSeasonHistoryPath.MEGA_ID))
        assertTrue(ui.contains("player_card_history_transfer_chip"))
        assertTrue(ui.contains("player_card_history_school_line"))
        assertTrue(root.resolve(PlayerPriorSeasonHistoryPath.REPO).readText().contains("schoolDisplayName"))
    }
}
