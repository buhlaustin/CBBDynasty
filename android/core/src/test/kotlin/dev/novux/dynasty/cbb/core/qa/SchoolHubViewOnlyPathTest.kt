package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolHubViewOnlyPathTest {
    @Test
    fun opponentSchoolHubShowsBanner() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/school/SchoolHubScreen.kt",
        ).readText()
        assertTrue(screen.contains("SchoolHubViewOnlyPolicy"))
        assertTrue(screen.contains("school_hub_view_only"))
    }
}