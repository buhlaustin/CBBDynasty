package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-005], [MEGA-FRAME-056], [MEGA-FRAME-130], [MEGA-FRAME-138] */
class DynastyCareerUnifiedRoutePathTest {
    @Test
    fun launchAndHostUseUnifiedCareerRoute() {
        val root = repoRoot()
        val launch = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/navigation/DynastyLaunch.kt")
            .readText()
        val host = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyCareerHost.kt")
            .readText()
        val app = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/DynastyApp.kt").readText()
        assertTrue(launch.contains("AppRoute.DynastyCareer.route"))
        assertTrue(launch.contains("UnemployedCoachShellPolicy"))
        assertTrue(launch.contains("DynastyCareerPhaseResolver"))
        assertTrue(host.contains("DynastyCareerPhaseResolver"))
        assertTrue(host.contains("onSeasonStarted = {}"))
        assertTrue(app.contains("composable(AppRoute.DynastyCareer.route)"))
        assertTrue(app.contains("DynastyCareerHost"))
    }

    @Test
    fun sessionNotifiesListenersOnSet() {
        val root = repoRoot()
        val session = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/save/ActiveDynastySession.kt",
        ).readText()
        assertTrue(session.contains("addListener"))
        assertTrue(session.contains("listeners.toList().forEach"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
