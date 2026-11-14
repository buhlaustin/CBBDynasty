package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PreseasonCompletenessPathTest {
    @Test
    fun preseasonScreenShowsTaskProgress() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonScreen.kt",
        ).readText()
        assertTrue(screen.contains("PreseasonTaskCompletenessPolicy"))
        assertTrue(screen.contains("preseason_tasks_progress"))
    }
}