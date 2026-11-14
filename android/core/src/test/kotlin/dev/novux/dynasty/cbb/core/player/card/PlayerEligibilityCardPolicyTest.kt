package dev.novux.dynasty.cbb.core.player.card

import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.PlayerEligibility
import dev.novux.dynasty.cbb.core.player.PlayerIdentity
import dev.novux.dynasty.cbb.core.player.PlayerInjuryState
import dev.novux.dynasty.cbb.core.player.PlayerRatings
import dev.novux.dynasty.cbb.core.player.PlayerRosterRole
import dev.novux.dynasty.cbb.core.player.archetype.PlayerArchetype
import dev.novux.dynasty.cbb.core.player.appearance.PlayerAppearance
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerEligibilityCardPolicyTest {
  @Test
    fun medicalRedshirtSeasonShowsMedicalLine() {
        val player = basePlayer().copy(
            eligibility = PlayerEligibility(
                yearsUsed = 1,
                yearsRemaining = 3,
                medicalRedshirtSeason = true,
            ),
        )
        assertTrue(
            PlayerEligibilityCardPolicy.overviewLineKinds(player)
                .contains(PlayerEligibilityCardPolicy.LineKind.MedicalRedshirt),
        )
    }

    @Test
    fun classAheadOfEligibilityClockShowsDevelopmentYear() {
        val player = basePlayer().copy(
            classYear = PlayerClassYear.Sophomore,
            eligibility = PlayerEligibility(yearsUsed = 0, yearsRemaining = 4),
        )
        assertTrue(
            PlayerEligibilityCardPolicy.overviewLineKinds(player)
                .contains(PlayerEligibilityCardPolicy.LineKind.DevelopmentYear),
        )
    }

    @Test
    fun extendedClockShowsFiveYearPath() {
        val player = basePlayer().copy(
            eligibility = PlayerEligibility(yearsUsed = 2, yearsRemaining = 3),
        )
        assertTrue(
            PlayerEligibilityCardPolicy.overviewLineKinds(player)
                .contains(PlayerEligibilityCardPolicy.LineKind.FiveYearPath),
        )
    }

    private fun basePlayer(): DynastyPlayer = DynastyPlayer(
        playerId = "p1",
        dynastyId = "d1",
        appearance = PlayerAppearance.fromPlayerId("p1"),
        identity = PlayerIdentity(
            displayName = "Test Player",
            hometownCity = "City",
            hometownState = "ST",
            heightInches = 78,
            weightLbs = 200,
            nilUsdPerYear = 0,
            nilDealRaisedThisSeason = false,
        ),
        position = PlayerPosition.SmallForward,
        classYear = PlayerClassYear.Freshman,
        ratings = PlayerRatings(ovr = 70, off = 70, def = 70),
        archetype = PlayerArchetype.WingScorer,
        eligibility = PlayerEligibility(yearsUsed = 0, yearsRemaining = 4),
        injury = PlayerInjuryState.Healthy,
        role = PlayerRosterRole(
            isStarter = false,
            depthOrder = 8,
            inTransferPortal = false,
            declaredForDraft = false,
            draftStockPoints = 0,
            proDayCompleted = false,
        ),
    )
}