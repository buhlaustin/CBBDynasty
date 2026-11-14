package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.pack.NewGameWizardPackSelectionPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewGameWizardPackSelectionPathTest {
    @Test
    fun settingsStepShowsActivePackLabel() {
        assertEquals("MEGA-GDD-087", NewGameWizardPackSelectionPolicy.MEGA_ID)
        val root = repoRoot()
        val wizard = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/newgame/NewGameWizardScreen.kt",
        ).readText()
        val settings = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/newgame/SettingsStepContent.kt",
        ).readText()
        assertTrue(wizard.contains("leagueDisplayName"))
        assertTrue(settings.contains("new_wizard_active_pack"))
        assertTrue(settings.contains("new_wizard_pack_selection_hint"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}