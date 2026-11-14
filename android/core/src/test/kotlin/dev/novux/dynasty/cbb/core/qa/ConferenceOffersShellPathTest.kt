package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class ConferenceOffersShellPathTest {
    @Test
    fun conferencePanelShowsShellHint() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val panel = root.resolve(ConferenceOffersShellPath.PANEL).readText()
        assertTrue(panel.contains("conference_offers_shell_hint"))
        assertTrue(panel.contains("ConferenceOffersShellPath"))
    }
}