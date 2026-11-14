package dev.novux.dynasty.cbb.core.player.games

import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.PlayerIdentity
import dev.novux.dynasty.cbb.core.player.PlayerRatings
import dev.novux.dynasty.cbb.core.player.archetype.PlayerArchetype
import dev.novux.dynasty.cbb.core.player.stats.PlayerCountingStats
import dev.novux.dynasty.cbb.core.player.stats.PlayerStatsSynth
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.player.PlayerEligibility
import dev.novux.dynasty.cbb.core.player.appearance.PlayerAppearance
import dev.novux.dynasty.cbb.core.player.PlayerInjuryState
import dev.novux.dynasty.cbb.core.player.PlayerRosterRole
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerGameLogSynthTest {
    @Test
    fun gameLines_sumToSeasonTotals() {
        val player = samplePlayer(gp = 12)
        val totals = PlayerStatsSynth.totalsForSeason(player, 12)
        val log = PlayerGameLogSynth.buildLog(player, seasonIndex = 1, gamesPlayed = 12, seasonTotals = totals)
        assertEquals(12, log.size)
        assertEquals(totals.points, log.sumOf { it.points })
        assertEquals(totals.assists, log.sumOf { it.assists })
        assertEquals(totals.rebounds, log.sumOf { it.rebounds })
    }

    private fun samplePlayer(gp: Int): DynastyPlayer = DynastyPlayer(
        playerId = "p-test-001",
        dynastyId = "d1",
        appearance = PlayerAppearance.fromPlayerId("p-test-001"),
        identity = PlayerIdentity(
            displayName = "Test Player",
            hometownCity = "Austin",
            hometownState = "TX",
            heightInches = 74,
            weightLbs = 195,
        ),
        position = PlayerPosition.ShootingGuard,
        classYear = PlayerClassYear.Sophomore,
        ratings = PlayerRatings.headline(82, 84, 78),
        archetype = PlayerArchetype.ThreePointSpecialist,
        eligibility = PlayerEligibility(yearsUsed = 1, yearsRemaining = 3),
        injury = PlayerInjuryState.Healthy,
        role = PlayerRosterRole(
            isStarter = true,
            depthOrder = 1,
            inTransferPortal = false,
            declaredForDraft = false,
            draftStockPoints = 0,
            proDayCompleted = false,
            rsGamesPlayedSeason = gp,
        ),
    )
}
