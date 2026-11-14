package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class DynastyShellTabRestorePathTest {
    @Test
    fun hubReturnRequestsDashboardTabAndShellObserves() {
        val root = repoRoot()
        val nav = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/navigation/DynastyNavigation.kt",
        ).readText()
        val session = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/save/ActiveDynastySession.kt",
        ).readText()
        val tabHook = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/rememberDynastyShellSelectedTab.kt",
        ).readText()
        assertTrue(nav.contains("requestShellDashboardTabOnHubReturn"))
        assertTrue(session.contains("shellDashboardTabRestoreNonce"))
        assertTrue(tabHook.contains("rememberDynastyShellSelectedTab"))
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
            ).readText().contains("rememberDynastyShellSelectedTab"),
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}