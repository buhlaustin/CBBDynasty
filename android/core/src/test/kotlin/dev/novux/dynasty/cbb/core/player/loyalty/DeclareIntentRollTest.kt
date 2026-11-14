package dev.novux.dynasty.cbb.core.player.loyalty

import dev.novux.dynasty.cbb.core.balance.BalanceLoyaltyTuning
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.PlayerEligibility
import dev.novux.dynasty.cbb.core.player.PlayerIdentity
import dev.novux.dynasty.cbb.core.player.PlayerInjuryState
import dev.novux.dynasty.cbb.core.player.PlayerRatings
import dev.novux.dynasty.cbb.core.player.PlayerRosterRole
import dev.novux.dynasty.cbb.core.player.archetype.PlayerArchetype
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertTrue
import org.junit.Test

class DeclareIntentRollTest {
    @Test
    fun highLoyaltyLowersDeclareProbabilityVersusLowLoyalty() {
        val tuning = BalanceLoyaltyTuning()
        val low = basePlayer(loyalty = 20)
        val high = basePlayer(loyalty = 88)
        val lowOdds = DeclareIntentRoll.declareProbability(low, mockDraftRank = 25, tuning)
        val highOdds = DeclareIntentRoll.declareProbability(high, mockDraftRank = 25, tuning)
        assertTrue(highOdds < lowOdds)
    }

    private fun basePlayer(loyalty: Int): DynastyPlayer = DynastyPlayer(
        playerId = "p-declare",
        dynastyId = "d1",
        appearance = dev.novux.dynasty.cbb.core.player.appearance.PlayerAppearance.fromPlayerId("p-declare"),
        identity = PlayerIdentity(
            displayName = "Star",
            hometownCity = "Dallas",
            hometownState = "TX",
            heightInches = 80,
            weightLbs = 210,
        ),
        position = PlayerPosition.SmallForward,
        classYear = PlayerClassYear.Junior,
        ratings = PlayerRatings(90, 92, 88, proPotential = 95),
        archetype = PlayerArchetype.WingScorer,
        eligibility = PlayerEligibility(2, 2, false),
        injury = PlayerInjuryState.Healthy,
        role = PlayerRosterRole(true, 1, false, false, 0, false),
        loyalty = PlayerLoyaltyProfile(
            LoyaltyRating(loyalty),
            LoyaltyPersonalityTag.ProgramGuy,
            CoachPromise(CoachPromiseType.StayPledgeTopN, stayTopN = 14, status = PromiseStatus.Kept),
        ),
    )
}
