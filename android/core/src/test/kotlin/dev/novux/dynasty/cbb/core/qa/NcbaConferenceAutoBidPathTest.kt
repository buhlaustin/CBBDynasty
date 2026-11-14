package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class NcbaConferenceAutoBidPathTest {
    @Test
    fun selectionSundayRepositoryUsesStandingsAutoBids() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val repo = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/selection/SelectionSundayRepository.kt",
        ).readText()
        assertTrue(repo.contains("NcbaConferenceAutoBidPolicy"))
    }
}