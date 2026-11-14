package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class WeekOneNilRetentionPathTest {
    @Test
    fun weekOnePanelWiresRetentionSheet() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(WeekOneNilRetentionPath.POLICY).readText().contains(WeekOneNilRetentionPath.MEGA_ID))
        assertTrue(root.resolve(WeekOneNilRetentionPath.PANEL).readText().contains("WeekOneNilRetentionSheet"))
    }
}