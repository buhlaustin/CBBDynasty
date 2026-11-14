package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.awards.AwardsHubPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class AwardsHubShellAccessPathTest {
    @Test
    fun shellMenuOpensAwardsHubWithBackHint() {
        val root = repoRoot()
        val menu = root.resolve(MENU).readText()
        val awards = root.resolve(AWARDS).readText()
        val policy = root.resolve(POLICY).readText()
        assertTrue(menu.contains("shell_menu_awards_hub"))
        assertTrue(menu.contains("onOpenAwards"))
        assertTrue(policy.contains(AwardsHubPolicy.SHELL_MENU_MEGA_ID))
        assertTrue(awards.contains("awards_hub_shell_access_hint"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }

    companion object {
        private const val MENU =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/hubs/DynastyHubMenu.kt"
        private const val AWARDS =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/awards/AwardsHubScreen.kt"
        private const val POLICY =
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/awards/AwardsHubPolicy.kt"
    }
}