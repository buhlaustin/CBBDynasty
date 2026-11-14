package dev.novux.dynasty.cbb.core.player

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayerSeed
import dev.novux.dynasty.cbb.core.player.appearance.PlayerAvatarPresets
import dev.novux.dynasty.cbb.core.roster.SeasonOneRosterGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerBootstrapFactoryTest {
    @Test
    fun seasonOneSeedsHaveRatingsAndNames() {
        val seeds = SeasonOneRosterGenerator.generate("dyn-1")
        assertTrue(seeds.all { it.displayName.isNotBlank() })
        assertTrue(seeds.all { it.ovr in 45..92 })
        assertTrue(seeds.all { PlayerRatings.RATING_RANGE.contains(it.off) })
    }

    @Test
    fun enrichSeed_assignsPackAppearanceIds() {
        val seeds = SeasonOneRosterGenerator.generate("dyn-appearance")
        assertTrue(seeds.all { it.skinToneId.isNotBlank() })
        assertTrue(seeds.all { PlayerAvatarPresets.isKnownPreset(it.avatarPresetId) })
    }

    @Test
    fun fromSeedProducesEligibleFreshman() {
        val seed = PlayerBootstrapFactory.enrichSeed(
            RosterPlayerSeed(
                playerId = "x",
                dynastyId = "d",
                position = PlayerPosition.PointGuard,
                classYear = PlayerClassYear.Freshman,
                isStarter = true,
                depthOrder = 1,
            ),
        )
        val player = PlayerBootstrapFactory.fromSeed(seed)
        assertEquals(0, player.eligibility.yearsUsed)
        assertEquals(4, player.eligibility.yearsRemaining)
    }
}
