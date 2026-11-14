package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePreviewSurfacePathTest {
    @Test
    fun gamePreviewScreenShowsSurfaceHint() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preview/GamePreviewScreen.kt",
        ).readText()
        assertTrue(screen.contains("game_preview_surface_hint"))
        assertTrue(screen.contains("game_preview_h2h_heading"))
    }
}