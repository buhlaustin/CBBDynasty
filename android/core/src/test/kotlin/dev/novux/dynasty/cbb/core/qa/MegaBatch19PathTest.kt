package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.dashboard.PortalPipelineEntrantDashboardPolicy
import dev.novux.dynasty.cbb.core.recruiting.ai.AiRecruitingInterestParityPolicy
import dev.novux.dynasty.cbb.core.shell.SplashPackErrorPolicy
import dev.novux.dynasty.cbb.core.shell.SplashReadyGatePolicy
import dev.novux.dynasty.cbb.core.shell.StudioBrandBackgroundPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MegaBatch19PathTest {
    @Test
    fun batch19RecruitAndShellWired() {
        assertEquals("MEGA-RECR-148", PortalPipelineEntrantDashboardPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-159", AiRecruitingInterestParityPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-006", StudioBrandBackgroundPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-011", SplashReadyGatePolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-012", SplashPackErrorPolicy.MEGA_ID)
        assertTrue(SplashReadyGatePolicy.isReady(packTeamCount = 350, saveSlotsInitialized = true))
        assertFalse(SplashReadyGatePolicy.isReady(packTeamCount = 0, saveSlotsInitialized = true))
        val root = repoRoot()
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
            ).readText().contains("PortalPipelineEntrantDashboardPolicy"),
        )
        assertTrue(
            root.resolve(
                "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/recruiting/ai/AiRecruitingWeekRunner.kt",
            ).readText().contains("AiRecruitingInterestParityPolicy"),
        )
        val studio = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/StudioScreen.kt",
        ).readText()
        assertTrue(studio.contains("StudioBrandBackgroundPolicy"))
        assertTrue(studio.contains("studio_tap_to_continue"))
        val splash = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/SplashScreen.kt",
        ).readText()
        assertTrue(splash.contains("SplashPackErrorPolicy"))
        assertTrue(splash.contains("splash_retry"))
        assertTrue(
            root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/DynastyApp.kt").readText()
                .contains("SplashReadyGatePolicy"),
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}