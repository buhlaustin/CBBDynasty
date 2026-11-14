package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerCardTeamHubPathTest {
    @Test
    fun playerOverviewTeamCardOpensSchoolHub() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val screen = root.resolve(PlayerCardTeamHubPath.PLAYER_CARD_SOURCE).readText()
        val shell = root.resolve(PlayerCardTeamHubPath.SHELL_SOURCE).readText()
        assertTrue(screen.contains(PlayerCardTeamHubPath.TEAM_CARD_COMPOSABLE))
        assertTrue(screen.contains("onOpenSchoolHub"))
        assertTrue(shell.contains("fullScreenPlayer"))
        assertTrue(shell.contains("schoolHubOverview = userSchool"))
        assertTrue(shell.contains("ShellCoachView.School"))
    }
}
