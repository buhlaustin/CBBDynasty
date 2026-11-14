package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachOverviewTeamCardPathTest {
    @Test
    fun overviewTeamCardWiredToSchoolHubInShell() {
        val root = repoRoot()
        val teamCard = root.resolve(CoachOverviewTeamCardPath.TEAM_CARD_SOURCE).readText()
        val screen = root.resolve(CoachOverviewTeamCardPath.SCREEN_SOURCE).readText()
        val shell = root.resolve(CoachOverviewTeamCardPath.SHELL_SOURCE).readText()
        assertTrue(teamCard.contains("clickable"))
        assertTrue(teamCard.contains("onOpenSchoolHub"))
        assertTrue(screen.contains(CoachOverviewTeamCardPath.OVERVIEW_COMPOSABLE))
        assertTrue(screen.contains("onOpenSchoolHub"))
        assertTrue(shell.contains("schoolHubOverview = userSchool"))
        assertTrue(shell.contains(CoachOverviewTeamCardPath.SHELL_SCHOOL_ROUTE))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
