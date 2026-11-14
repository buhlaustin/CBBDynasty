package dev.novux.dynasty.cbb.core.news

import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.injuries.InjurySeverity
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertEquals
import org.junit.Test

class UserInjuryNewsGateTest {
    @Test
    fun majorInjuryAlwaysNewsworthy() {
        val event = GameSimInjuryEvent(
            playerId = "p1",
            injuryId = "acl",
            label = "ACL",
            bodyPart = "knee",
            severity = InjurySeverity.Major,
            weeksRemaining = 8,
        )
        val players = listOf(
            RosterPlayer(
                playerId = "p1",
                dynastyId = "d",
                position = PlayerPosition.PointGuard,
                classYear = PlayerClassYear.Senior,
                isStarter = true,
                depthOrder = 1,
                inTransferPortal = false,
                ovr = 70,
            ),
        )
        assertEquals(1, UserInjuryNewsGate.newsworthy(listOf(event), players, headlineOverallMin = 78).size)
    }
}
