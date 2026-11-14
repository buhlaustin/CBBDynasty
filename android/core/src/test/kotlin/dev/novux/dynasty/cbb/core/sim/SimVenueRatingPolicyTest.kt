package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimVenueRatingPolicyTest {
  @Test
    fun homeOffenseBoostedAtHomeVenue() {
        val mods = SimVenueModifiers(home = 1.05, neutral = 1.0, road = 0.95)
        val boosted = SimVenueRatingPolicy.offensiveRating(80, offenseIsHome = true, SimHomeVenueType.Home, mods)
        assertEquals(84, boosted)
    }

    @Test
    fun awayOffenseUsesRoadMultiplierAtHomeGame() {
        val mods = SimVenueModifiers(home = 1.05, neutral = 1.0, road = 0.95)
        val adjusted = SimVenueRatingPolicy.offensiveRating(80, offenseIsHome = false, SimHomeVenueType.Home, mods)
        assertEquals(76, adjusted)
    }

    @Test
    fun lateGameCrowdAddsOneWhenHomeLate() {
        assertEquals(1, SimVenueRatingPolicy.lateGameHomeCrowdOffenseBoost(true, 90, SimHomeVenueType.Home))
        assertEquals(0, SimVenueRatingPolicy.lateGameHomeCrowdOffenseBoost(true, 200, SimHomeVenueType.Home))
    }

    @Test
    fun boostedRatingNeverBelowOne() {
        val tiny = SimVenueRatingPolicy.offensiveRating(1, true, SimHomeVenueType.Home, SimVenueModifiers(0.01, 1.0, 1.0))
        assertTrue(tiny >= 1)
    }
}
