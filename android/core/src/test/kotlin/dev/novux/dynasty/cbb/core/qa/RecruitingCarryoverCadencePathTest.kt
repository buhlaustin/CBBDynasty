package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.recruiting.RecruitingCarryoverCadencePolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitingCarryoverCadencePathTest {
    @Test
    fun hubSurfacesCarryoverHint() {
        assertEquals("MEGA-GDD-115", RecruitingCarryoverCadencePolicy.MEGA_ID)
        val hub = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/recruiting/RecruitingHubScreen.kt",
        ).readText()
        assertTrue(hub.contains("recruiting_carryover_cadence_hint"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}