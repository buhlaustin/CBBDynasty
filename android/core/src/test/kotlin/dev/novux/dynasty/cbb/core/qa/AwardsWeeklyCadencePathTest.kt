package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class AwardsWeeklyCadencePathTest {
    @Test
    fun weeklyAwardsScreenShowsCadenceHint() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val hub = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/awards/AwardsHubScreen.kt",
        ).readText()
        assertTrue(hub.contains("AwardsWeeklyCadencePolicy"))
        assertTrue(hub.contains("awards_weekly_cadence_hint"))
    }
}