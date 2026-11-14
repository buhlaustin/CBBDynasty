package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class NewGameWizardSeasonPathTest {
    @Test
    fun settingsStepShowsStartingSeason() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val settings = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/newgame/SettingsStepContent.kt",
        ).readText()
        assertTrue(settings.contains("NewGameWizardSeasonPolicy"))
        assertTrue(settings.contains("new_settings_starting_season"))
    }
}