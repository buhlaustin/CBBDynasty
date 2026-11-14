package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class LineupRotationCadencePathTest {
    @Test
    fun strategyRotationShowsSlotHint() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val panel = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/strategy/StrategyRotationMinutesPanel.kt",
        ).readText()
        assertTrue(panel.contains("LineupRotationCadencePolicy"))
        assertTrue(panel.contains("strategy_rotation_slots_hint"))
    }
}