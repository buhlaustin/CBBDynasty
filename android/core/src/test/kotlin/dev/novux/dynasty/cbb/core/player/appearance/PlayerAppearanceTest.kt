package dev.novux.dynasty.cbb.core.player.appearance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerAppearanceTest {
    @Test
    fun fromPlayerId_isStableAndKnownPreset() {
        val a = PlayerAppearance.fromPlayerId("dynasty-player-7")
        val b = PlayerAppearance.fromPlayerId("dynasty-player-7")
        assertEquals(a, b)
        assertTrue(PlayerAvatarPresets.isKnownPreset(a.avatarPresetId))
    }

    @Test
    fun compositeMatchesPackTable() {
        val appearance = PlayerAppearance(
            skinToneId = "skin_2",
            hairColorId = "hair_blonde",
            hairStyleId = "hair_curly",
        )
        assertEquals(
            "player-skin_2-hair_blonde-hair_curly",
            appearance.avatarPresetId,
        )
    }
}
