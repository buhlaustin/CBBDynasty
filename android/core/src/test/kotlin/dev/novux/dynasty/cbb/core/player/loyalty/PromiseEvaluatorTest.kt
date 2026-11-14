package dev.novux.dynasty.cbb.core.player.loyalty

import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.PlayerEligibility
import dev.novux.dynasty.cbb.core.player.PlayerIdentity
import dev.novux.dynasty.cbb.core.player.PlayerInjuryState
import dev.novux.dynasty.cbb.core.player.PlayerRatings
import dev.novux.dynasty.cbb.core.player.PlayerRosterRole
import dev.novux.dynasty.cbb.core.player.archetype.PlayerArchetype
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Test

class PromiseEvaluatorTest {
  private fun player(starter: Boolean): DynastyPlayer = DynastyPlayer(
        playerId = "p1",
        dynastyId = "d1",
        appearance = dev.novux.dynasty.cbb.core.player.appearance.PlayerAppearance.fromPlayerId("p1"),
        identity = PlayerIdentity(
            displayName = "Test",
            hometownCity = "Columbus",
            hometownState = "OH",
            heightInches = 76,
            weightLbs = 200,
        ),
        position = PlayerPosition.ShootingGuard,
        classYear = PlayerClassYear.Junior,
        ratings = PlayerRatings.headline(75, 76, 72),
        archetype = PlayerArchetype.ComboGuard,
        eligibility = PlayerEligibility(yearsUsed = 2, yearsRemaining = 2, medicalRedshirtSeason = false),
        injury = PlayerInjuryState.Healthy,
        role = PlayerRosterRole(
            isStarter = starter,
            depthOrder = if (starter) 1 else 10,
            inTransferPortal = false,
            declaredForDraft = false,
            draftStockPoints = 0,
            proDayCompleted = false,
        ),
        loyalty = PlayerLoyaltyProfile(
            loyalty = LoyaltyRating(60),
            personality = LoyaltyPersonalityTag.ProgramGuy,
            promise = CoachPromise(type = CoachPromiseType.MinutesStarter),
        ),
    )

    @Test
    fun starterPromiseKeptWhenPlayerStarts() {
        val status = PromiseEvaluator.evaluateSeasonWindow(player(starter = true), seasonMinutesPerGame = null)
        assertEquals(PromiseStatus.Kept, status)
    }

    @Test
    fun starterPromiseBrokenWhenBenched() {
        val status = PromiseEvaluator.evaluateSeasonWindow(player(starter = false), seasonMinutesPerGame = null)
        assertEquals(PromiseStatus.Broken, status)
    }

    @Test
    fun starterMinutes_grayMpg_isAtRisk() {
        val eval = PromiseEvaluator.evaluateSeasonWindow(player(starter = true), seasonMinutesPerGame = 22.0)
        assertEquals(PromiseStatus.AtRisk, eval)
    }
}
