package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarouselAiBehaviorPathTest {
    @Test
    fun carouselAiEnginesWireGddBehaviorTable() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(CarouselAiBehaviorPath.BEHAVIOR_TABLE).readText().contains(CarouselAiBehaviorPath.MEGA_ID))
        assertTrue(root.resolve(CarouselAiBehaviorPath.LEAGUE_SIM).readText().contains(CarouselAiBehaviorPath.MEGA_ID))
        val engine = root.resolve(CarouselAiBehaviorPath.ENGINE).readText()
        assertTrue(engine.contains(CarouselAiBehaviorPath.MEGA_ID))
        assertTrue(engine.contains("CarouselLeagueBackgroundSimEngine"))
        assertFalse(engine.contains("stubUserApprovalCards"))
    }
}
