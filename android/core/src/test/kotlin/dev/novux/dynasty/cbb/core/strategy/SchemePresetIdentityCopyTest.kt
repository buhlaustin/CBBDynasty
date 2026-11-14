package dev.novux.dynasty.cbb.core.strategy

import org.junit.Assert.assertTrue
import org.junit.Test

class SchemePresetIdentityCopyTest {
    @Test
    fun offensiveCopyMentionsPerimeterIdentity() {
        val copy = SchemePresetIdentityCopy.offensive(OffensiveSchemePreset.Perimeter)
        assertTrue(copy.contains("3PA"))
    }

    @Test
    fun defensiveCopyMentionsPressFatigue() {
        val copy = SchemePresetIdentityCopy.defensive(DefensiveSchemePreset.FullCourtPress)
        assertTrue(copy.contains("fatigue", ignoreCase = true))
    }
}
