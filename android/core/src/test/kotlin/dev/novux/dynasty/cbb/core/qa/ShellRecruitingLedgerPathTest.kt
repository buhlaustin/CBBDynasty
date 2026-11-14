package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-096] */
class ShellRecruitingLedgerPathTest {
    @Test
    fun shell_loadsRecruitingLedgerAndDashContextTogether() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt",
        ).readText()
        assertTrue(screen.contains(RecruitingDashboardFirstPaint.SHELL_LEDGER_CONTEXT_EFFECT_MARKER))
        assertTrue(screen.contains("recruitingEconomyRepository.loadOrOpenCycle"))
        assertTrue(screen.contains("InSeasonRecruitingDashboardContext"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}