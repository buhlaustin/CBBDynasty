package dev.novux.dynasty.cbb.core.player.card

import dev.novux.dynasty.cbb.core.player.PlayerRosterRole
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerRosterStatusCardPolicyTest {
    @Test
    fun portalAndDeclareChipsOrdered() {
        val role = PlayerRosterRole(
            isStarter = true,
            depthOrder = 1,
            inTransferPortal = true,
            declaredForDraft = true,
            draftStockPoints = 42,
            proDayCompleted = true,
        )
        val chips = PlayerRosterStatusCardPolicy.visibleChips(
            dev.novux.dynasty.cbb.core.player.DynastyPlayer(
                playerId = "p",
                dynastyId = "d",
                appearance = dev.novux.dynasty.cbb.core.player.appearance.PlayerAppearance.fromPlayerId("p"),
                identity = dev.novux.dynasty.cbb.core.player.PlayerIdentity(
                    displayName = "P",
                    hometownCity = "C",
                    hometownState = "S",
                    heightInches = 76,
                    weightLbs = 190,
                    nilUsdPerYear = 0,
                    nilDealRaisedThisSeason = false,
                ),
                position = dev.novux.dynasty.cbb.core.roster.PlayerPosition.PointGuard,
                classYear = dev.novux.dynasty.cbb.core.roster.PlayerClassYear.Junior,
                ratings = dev.novux.dynasty.cbb.core.player.PlayerRatings(70, 70, 70),
                archetype = dev.novux.dynasty.cbb.core.player.archetype.PlayerArchetype.FloorGeneral,
                eligibility = dev.novux.dynasty.cbb.core.player.PlayerEligibility(2, 2),
                injury = dev.novux.dynasty.cbb.core.player.PlayerInjuryState.Healthy,
                role = role,
            ),
        )
        assertEquals(
            listOf(
                PlayerRosterStatusCardPolicy.ChipKind.TransferPortal,
                PlayerRosterStatusCardPolicy.ChipKind.DeclaredForDraft,
                PlayerRosterStatusCardPolicy.ChipKind.ProDayComplete,
                PlayerRosterStatusCardPolicy.ChipKind.DraftStock,
            ),
            chips,
        )
    }
}