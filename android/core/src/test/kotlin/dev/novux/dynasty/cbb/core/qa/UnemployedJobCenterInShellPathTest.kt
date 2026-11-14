package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.coach.employment.UnemployedCoachShellPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-144] */
class UnemployedJobCenterInShellPathTest {
    @Test
    fun unemployedLaunchAndHostUseCareerShell() {
        val root = repoRoot()
        val launch = root.resolve(LAUNCH).readText()
        val host = root.resolve(HOST).readText()
        val screen = root.resolve(SCREEN).readText()
        assertTrue(launch.contains("UnemployedCoachShellPolicy"))
        assertTrue(launch.contains("AppRoute.DynastyCareer.route"))
        assertTrue(!launch.contains("AppRoute.JobCenter.route"))
        assertTrue(host.contains("UnemployedCoachCareerScreen"))
        assertTrue(screen.contains("DynastyShellChrome"))
        assertTrue(screen.contains("job_center_shell_header_hint"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }

    companion object {
        private const val LAUNCH =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/navigation/DynastyLaunch.kt"
        private const val HOST =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyCareerHost.kt"
        private const val SCREEN =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/coach/UnemployedCoachCareerScreen.kt"
    }
}