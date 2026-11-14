package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-042] */
class GamePreviewPrimaryCtaPathTest {
    @Test
    fun gamePreview_prominentStrategyPrimaryCta() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preview/GamePreviewScreen.kt",
        ).readText()
        assertTrue(screen.contains("game_preview_primary_cta_heading"))
        assertTrue(screen.contains("Button(onClick = onOpenStrategy"))
        assertTrue(!screen.contains("OutlinedButton(onClick = onOpenStrategy"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
