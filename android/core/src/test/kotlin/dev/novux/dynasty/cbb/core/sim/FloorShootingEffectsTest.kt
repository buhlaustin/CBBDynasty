package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.strategy.ResolvedSchemeEffects
import org.junit.Assert.assertTrue
import org.junit.Test

class FloorShootingEffectsTest {
    private val base = ResolvedSchemeEffects(
        tripPaceScale = 1.0,
        threePointAttemptRate = 0.35,
        turnoverChance = 0.22,
        makeRateAdjust = 0.0,
        offensiveRatingDelta = 0,
        defensiveRatingDelta = 0,
    )

    @Test
    fun highThreePointTendencyRaisesAttemptRateAndMakeNudge() {
        val shooters = List(5) {
            GameSimParticipant(
                playerId = "p$it",
                listedPosition = PlayerPosition.ShootingGuard,
                slotPlayed = PlayerPosition.ShootingGuard,
                offensiveRating = 80,
                defensiveRating = 75,
                targetMinutes = 28,
                shooting = GameSimShootingProfile(threePointTendency = 88),
            )
        }
        val merged = FloorShootingEffects.merge(base, shooters)
        assertTrue(merged.threePointAttemptRate > base.threePointAttemptRate)
        assertTrue(merged.makeRateAdjust > base.makeRateAdjust)
    }

    @Test
    fun lowThreePointTendencyLowersAttemptRate() {
        val nonShooters = List(5) {
            GameSimParticipant(
                playerId = "p$it",
                listedPosition = PlayerPosition.Center,
                slotPlayed = PlayerPosition.Center,
                offensiveRating = 70,
                defensiveRating = 72,
                targetMinutes = 24,
                shooting = GameSimShootingProfile(threePointTendency = 18),
            )
        }
        val merged = FloorShootingEffects.merge(base, nonShooters)
        assertTrue(merged.threePointAttemptRate < base.threePointAttemptRate)
    }
}
