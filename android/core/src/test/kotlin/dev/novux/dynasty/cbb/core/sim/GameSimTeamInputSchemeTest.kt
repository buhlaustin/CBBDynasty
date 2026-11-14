package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset
import dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset
import org.junit.Assert.assertEquals
import org.junit.Test

class GameSimTeamInputSchemeTest {
    @Test
    fun teamInputPreservesPerGameSchemes() {
        val home = GameSimTeamInput(
            teamId = "us",
            offensiveRating = 78,
            defensiveRating = 74,
            offensiveScheme = OffensiveSchemePreset.Perimeter,
            defensiveScheme = DefensiveSchemePreset.FullCourtPress,
        )
        assertEquals(OffensiveSchemePreset.Perimeter, home.offensiveScheme)
        assertEquals(DefensiveSchemePreset.FullCourtPress, home.defensiveScheme)
    }
}
