package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachingCarouselShellPathTest {
    @Test
    fun carouselScreenUsesDynastyShellChrome() {
        val root = repoRoot()
        val screen = root.resolve(CoachingCarouselShellPath.SCREEN).readText()
        assertTrue(screen.contains(CoachingCarouselShellPath.MEGA_ID))
        assertTrue(screen.contains("DynastyShellChrome"))
        assertTrue(screen.contains("DynastyShellTab"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
