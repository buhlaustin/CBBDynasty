package dev.novux.dynasty.cbb.core.portrait

import dev.novux.dynasty.cbb.core.player.appearance.PlayerAppearanceResolver
import org.junit.Assert.assertEquals
import org.junit.Test

class PresetPortraitRendererTest {
    @Test
    fun samePresetId_producesSameSignatureHash() {
        val id = "player-skin_3-hair_black-hair_fade"
        val a = PresetPortraitRenderer.specFor(id)
        val b = PresetPortraitRenderer.specFor(id)
        assertEquals(a.signatureHash, b.signatureHash)
        assertEquals(a.skinToneArgb, b.skinToneArgb)
        assertEquals(a.hairColorArgb, b.hairColorArgb)
    }

    @Test
    fun playerResolver_isStableForPlayerId() {
        val id1 = PlayerAppearanceResolver.presetIdForPlayer("player-abc")
        val id2 = PlayerAppearanceResolver.presetIdForPlayer("player-abc")
        assertEquals(id1, id2)
        assertEquals(
            PresetPortraitRenderer.specFor(id1).signatureHash,
            PresetPortraitRenderer.specFor(id2).signatureHash,
        )
    }
}
