package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.awards.ConferenceWeeklyAwardsPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConferenceWeeklyAwardsPathTest {
    @Test
    fun weeklyScreenConferenceHonorsHint() {
        assertEquals("MEGA-GDD-131", ConferenceWeeklyAwardsPolicy.MEGA_ID)
        val hub = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/awards/AwardsHubScreen.kt",
        ).readText()
        assertTrue(hub.contains("ConferenceWeeklyAwardsPolicy"))
        assertTrue(hub.contains("awards_weekly_conference_hint"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}