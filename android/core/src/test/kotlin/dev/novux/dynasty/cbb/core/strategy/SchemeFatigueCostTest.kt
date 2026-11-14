package dev.novux.dynasty.cbb.core.strategy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SchemeFatigueCostTest {
    @Test
    fun pressAddsExtraDrain() {
        val press = SchemeFatigueCost.extraDefensiveDrainSeconds(
            DefensiveSchemePreset.FullCourtPress,
            tripSeconds = 20,
        )
        val man = SchemeFatigueCost.extraDefensiveDrainSeconds(
            DefensiveSchemePreset.ManToMan,
            tripSeconds = 20,
        )
        assertEquals(0, man)
        assertTrue(press >= 2)
    }

    @Test
    fun boxAndOneAddsModerateDrain() {
        val drain = SchemeFatigueCost.extraDefensiveDrainSeconds(
            DefensiveSchemePreset.BoxAndOne,
            tripSeconds = 15,
        )
        assertTrue(drain in 1..3)
    }
}
