package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class NewGameContractNilPreviewPathTest {
    @Test
    fun signContractStepShowsNilPreview() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val wizard = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/newgame/NewGameWizardScreen.kt",
        ).readText()
        val sign = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/newgame/SignContractStepContent.kt",
        ).readText()
        assertTrue(wizard.contains("NewGameSchoolSummaryPreviewEngine.build"))
        assertTrue(sign.contains("NewGameContractNilPreviewPolicy"))
        assertTrue(sign.contains("new_sign_nil_preview"))
    }
}