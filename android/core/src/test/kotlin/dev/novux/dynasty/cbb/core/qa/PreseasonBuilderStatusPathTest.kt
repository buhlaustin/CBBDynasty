package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PreseasonBuilderStatusPathTest {
    @Test
    fun preseasonSurfacesBuilderValidationOnChecklist() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonScreen.kt",
        ).readText()
        assertTrue(screen.contains("preseasonBuilderStatusBanner"))
        assertTrue(screen.contains("onDraftIssueChanged"))
        assertTrue(screen.contains("onValidationChanged"))
        assertTrue(screen.contains("preseason_task_order_hint"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}