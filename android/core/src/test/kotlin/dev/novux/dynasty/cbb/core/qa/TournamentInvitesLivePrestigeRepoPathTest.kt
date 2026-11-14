package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.preseason.tournament.TournamentInvitesLivePrestigePolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TournamentInvitesLivePrestigeRepoPathTest {
    @Test
    fun preseasonDataLayerPassesLivePrestige() {
        assertEquals("MEGA-GDD-104", TournamentInvitesLivePrestigePolicy.MEGA_ID)
        val root = repoRoot()
        val schedule = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/preseason/PreseasonScheduleRepository.kt",
        ).readText()
        val bracket = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/preseason/EarlySeasonBracketRepository.kt",
        ).readText()
        assertTrue(schedule.contains("liveProgramPrestige"))
        assertTrue(schedule.contains("resolveProgramPrestigeForDynasty"))
        assertTrue(bracket.contains("resolveProgramPrestigeForDynasty"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}