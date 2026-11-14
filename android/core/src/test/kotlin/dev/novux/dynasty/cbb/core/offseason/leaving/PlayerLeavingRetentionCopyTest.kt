package dev.novux.dynasty.cbb.core.offseason.leaving

import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.PlayerEligibility
import dev.novux.dynasty.cbb.core.player.PlayerIdentity
import dev.novux.dynasty.cbb.core.player.PlayerInjuryState
import dev.novux.dynasty.cbb.core.player.PlayerRatings
import dev.novux.dynasty.cbb.core.player.PlayerRosterRole
import dev.novux.dynasty.cbb.core.player.archetype.PlayerArchetype
import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromise
import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromiseType
import dev.novux.dynasty.cbb.core.player.loyalty.LoyaltyPersonalityTag
import dev.novux.dynasty.cbb.core.player.loyalty.LoyaltyRating
import dev.novux.dynasty.cbb.core.player.loyalty.PlayerLoyaltyProfile
import dev.novux.dynasty.cbb.core.player.loyalty.PromiseStatus
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerLeavingRetentionCopyTest {
    @Test
    fun retentionModel_exposesLoyaltyAndPromiseType() {
        val player = samplePlayer(
            loyalty = 68,
            promise = CoachPromise(
                type = CoachPromiseType.MinutesRotation,
                status = PromiseStatus.AtRisk,
            ),
        )
        assertEquals(68, player.loyalty.loyalty.value)
        assertEquals("Rotation minutes", player.loyalty.promise!!.displayLabel())
        assertEquals(PromiseStatus.AtRisk, player.loyalty.promise!!.status)
        assertTrue(PlayerLeavingRetentionPolicy.MEGA_ID.isNotBlank())
    }

    private fun samplePlayer(loyalty: Int, promise: CoachPromise?): DynastyPlayer = DynastyPlayer(
        playerId = "p-ret",
        dynastyId = "d1",
        appearance = dev.novux.dynasty.cbb.core.player.appearance.PlayerAppearance.fromPlayerId("p-ret"),
        identity = PlayerIdentity(
            displayName = "Test",
            hometownCity = "Columbus",
            hometownState = "OH",
            heightInches = 76,
            weightLbs = 200,
        ),
        position = PlayerPosition.PowerForward,
        classYear = PlayerClassYear.Senior,
        ratings = PlayerRatings.headline(80, 78, 76),
        archetype = PlayerArchetype.StretchFour,
        eligibility = PlayerEligibility(yearsUsed = 3, yearsRemaining = 1, medicalRedshirtSeason = false),
        injury = PlayerInjuryState.Healthy,
        role = PlayerRosterRole(
            isStarter = true,
            depthOrder = 1,
            inTransferPortal = false,
            declaredForDraft = false,
            draftStockPoints = 0,
            proDayCompleted = false,
        ),
        loyalty = PlayerLoyaltyProfile(
            loyalty = LoyaltyRating(loyalty),
            personality = LoyaltyPersonalityTag.ProgramGuy,
            promise = promise,
        ),
    )
}
