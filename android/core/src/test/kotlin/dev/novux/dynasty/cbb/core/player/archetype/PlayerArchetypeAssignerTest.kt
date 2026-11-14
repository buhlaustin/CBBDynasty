package dev.novux.dynasty.cbb.core.player.archetype

import dev.novux.dynasty.cbb.core.player.PlayerRatings
import dev.novux.dynasty.cbb.core.player.scout.ScoutDepth
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerArchetypeAssignerTest {
    @Test
    fun assignsThreePointSpecialist() {
        val ratings = PlayerRatings(
            ovr = 80,
            off = 82,
            def = 70,
            shootingThree = 92,
            shootingMid = 88,
            shootingInside = 70,
            handling = 68,
            passing = 65,
        )
        val archetype = PlayerArchetypeAssigner.assign(PlayerPosition.ShootingGuard, ratings)
        assertEquals(PlayerArchetype.ThreePointSpecialist, archetype)
    }

    @Test
    fun scoutReportUsesLettersNotInts() {
        val ratings = PlayerRatings.headline(88, 90, 75)
        val spread = ratings.copy(shootingThree = 90, handling = 85)
        val archetype = PlayerArchetypeAssigner.assign(PlayerPosition.SmallForward, spread)
        val player = dev.novux.dynasty.cbb.core.player.DynastyPlayer(
            playerId = "p",
            dynastyId = "d",
            appearance = dev.novux.dynasty.cbb.core.player.appearance.PlayerAppearance.fromPlayerId("p"),
            identity = dev.novux.dynasty.cbb.core.player.PlayerIdentity(
                "Test",
                "City",
                "ST",
                78,
                200,
            ),
            position = PlayerPosition.SmallForward,
            classYear = dev.novux.dynasty.cbb.core.roster.PlayerClassYear.Junior,
            ratings = spread,
            archetype = archetype,
            eligibility = dev.novux.dynasty.cbb.core.player.PlayerEligibility.forClassYear(
                dev.novux.dynasty.cbb.core.roster.PlayerClassYear.Junior,
            ),
            injury = dev.novux.dynasty.cbb.core.player.PlayerInjuryState.Healthy,
            role = dev.novux.dynasty.cbb.core.player.PlayerRosterRole(
                isStarter = true,
                depthOrder = 1,
                inTransferPortal = false,
                declaredForDraft = false,
                draftStockPoints = 0,
                proDayCompleted = false,
            ),
        )
        val report = player.scoutReport(ScoutDepth.Full)
        assertEquals(true, report.overall.first().isLetter())
        assertEquals(archetype, report.archetype)
    }
}
