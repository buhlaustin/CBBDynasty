package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.coach.card.CoachPreviewViewMoreRoutePolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachPreviewViewMoreRoutePathTest {
    @Test
    fun viewMorePassesTargetAndOpensCoachCardRoute() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val sheet = root.resolve(CoachPreviewViewMoreRoutePath.SHEET).readText()
        assertTrue(sheet.contains(CoachPreviewViewMoreRoutePath.MEGA_ID))
        assertTrue(sheet.contains("onViewMore: (CoachPreviewTarget)"))
        assertTrue(sheet.contains("onViewMore(target)"))
        val shell = root.resolve(CoachPreviewViewMoreRoutePath.SHELL).readText()
        assertTrue(shell.contains("onViewMore = { target"))
        assertTrue(shell.contains(CoachPreviewViewMoreRoutePolicy.SHELL_OPEN_FULL))
        assertTrue(shell.contains(CoachPreviewViewMoreRoutePolicy.ROUTE_SYMBOL))
        assertTrue(shell.contains("withTab(CoachCardTab.Overview)"))
    }
}
