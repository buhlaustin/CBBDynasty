package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerGameBoxScoreSheetPathTest {
    @Test
    fun gameRowOpensSimSourcedBoxScoreSheet() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(PlayerGameBoxScoreSheetPath.ENGINE).readText().contains(PlayerGameBoxScoreSheetPath.MEGA_ID))
        assertTrue(root.resolve(PlayerGameBoxScoreSheetPath.PANEL).readText().contains("clickable"))
        assertTrue(root.resolve(PlayerGameBoxScoreSheetPath.SHEET).readText().contains("PlayerGameBoxScoreSheet"))
        assertTrue(root.resolve(PlayerGameBoxScoreSheetPath.REPO).readText().contains("ingestCommittedSimMarks"))
    }
}
