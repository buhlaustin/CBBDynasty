package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachCardContractTabShellPathTest {
    @Test
    fun contractTabShellHostsSalaryBuyoutGoalsBlocks() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val shell = root.resolve(CoachCardContractTabShellPath.SHELL).readText()
        assertTrue(shell.contains(CoachCardContractTabShellPath.SHELL_COMPOSABLE))
        assertTrue(shell.contains(CoachCardContractTabShellPath.TERMS_BLOCK))
        assertTrue(shell.contains(CoachCardContractTabShellPath.GOALS_BLOCK))
        assertTrue(shell.contains("CoachCardContractTabPolicy"))
        val hcTab = root.resolve(CoachCardContractTabShellPath.HC_TAB).readText()
        assertTrue(hcTab.contains("CoachCardContractTabShell"))
        assertTrue(hcTab.contains("CoachContractTabGoalsBlock"))
        assertTrue(root.resolve(CoachCardContractTabShellPath.HEAD_SCREEN).readText().contains("HeadCoachContractTab"))
    }
}
