package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-141] */
class WeekOneLeavingInShellPathTest {
    @Test
    fun playersLeaving_embeddedInOffseasonShell_notSeparateRoute() {
        val root = repoRoot()
        val offseason = root.resolve(OFFSEASON).readText()
        val routes = root.resolve(APP_ROUTE).readText()
        assertTrue(offseason.contains("PlayersLeavingPanel"))
        assertTrue(!routes.contains("PlayersLeaving"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }

    companion object {
        private const val OFFSEASON =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt"
        private const val APP_ROUTE =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/navigation/AppRoute.kt"
    }
}