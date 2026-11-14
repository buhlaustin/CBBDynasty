package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-004] */
class DynastyShellDrawerPathTest {
    @Test
    fun shellChrome_usesSlideDrawerNotDropdown() {
        val root = repoRoot()
        val chrome = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellChrome.kt",
        ).readText()
        assertTrue(chrome.contains("ModalNavigationDrawer"))
        assertTrue(chrome.contains("NavigationDrawerItem"))
        assertFalse(chrome.contains("DropdownMenu"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
