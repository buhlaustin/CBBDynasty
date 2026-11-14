package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class DynastyAutomationPriorityPathTest {
    @Test
    fun automationScreenShowsPriorityHint() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyAutomationScreen.kt",
        ).readText()
        assertTrue(screen.contains("DynastyAutomationPriorityPolicy"))
        assertTrue(screen.contains("dynasty_automation_priority_hint"))
    }
}