package dev.novux.dynasty.cbb.core.injuries

import dev.novux.dynasty.cbb.core.balance.BalanceMedicalRedshirtRules
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicalRedshirtPolicyTest {
    private val rules = BalanceMedicalRedshirtRules()

    @Test
    fun moderateInjuryBeforeWeek21Eligible() {
        val player = injuredPlayer(severity = "moderate", rsGames = 10, gameIndex = 12)
        val result = MedicalRedshirtPolicy.evaluate(player, rsWeekIndex = 14, rules)
        assertTrue(result.eligible)
    }

    @Test
    fun confirmationDetailIncludesGamesPlayedAndScheduleIndex() {
        val player = injuredPlayer(severity = "major", rsGames = 8, gameIndex = 9)
        val detail = MedicalRedshirtPolicy.confirmationDetail(player, rules)
        assertTrue(detail.contains("8"))
        assertTrue(detail.contains("9"))
    }

    @Test
    fun minorInjuryNotEligible() {
        val player = injuredPlayer(severity = "minor", rsGames = 5, gameIndex = 5)
        val result = MedicalRedshirtPolicy.evaluate(player, rsWeekIndex = 10, rules)
        assertFalse(result.eligible)
    }

    @Test
    fun exhibitionGameNeverEligible() {
        val player = injuredPlayer(severity = "major", rsGames = 4, gameIndex = 6)
        val result = MedicalRedshirtPolicy.evaluate(
            player,
            rsWeekIndex = 10,
            rules,
            countsTowardMedicalRedshirt = false,
        )
        assertFalse(result.eligible)
        assertTrue(result.reason == "exhibition_game")
    }

    private fun injuredPlayer(severity: String, rsGames: Int, gameIndex: Int): RosterPlayer =
        RosterPlayer(
            playerId = "p1",
            dynastyId = "d",
            position = PlayerPosition.PowerForward,
            classYear = PlayerClassYear.Freshman,
            isStarter = true,
            depthOrder = 4,
            inTransferPortal = false,
            injuryActive = true,
            injuryId = "knee_sprain",
            injuryWeeksRemaining = 6,
            injuryLabel = "Knee sprain",
            injurySeverityWire = severity,
            injuryOccurredAtRsGameIndex = gameIndex,
            rsGamesPlayedSeason = rsGames,
        )
}
