package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.preseason.tournament.TournamentInvitesPrestigePolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-074] */
class TournamentInvitesPrestigePanelPathTest {
    @Test
    fun panelUsesLivePrestigeAndMultiInviteCards() {
        val root = repoRoot()
        val panel = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/TournamentInvitesPanel.kt",
        ).readText()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonScreen.kt",
        ).readText()
        assertEquals("MEGA-FRAME-074", TournamentInvitesPrestigePolicy.MEGA_FRAME_ID)
        assertTrue(panel.contains("programPrestige"))
        assertTrue(panel.contains("TournamentInviteEventCard"))
        assertTrue(panel.contains("ElevatedCard"))
        assertTrue(panel.contains("TournamentInvitesPrestigePolicy"))
        assertTrue(screen.contains("resolveUserProgramPrestige"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
