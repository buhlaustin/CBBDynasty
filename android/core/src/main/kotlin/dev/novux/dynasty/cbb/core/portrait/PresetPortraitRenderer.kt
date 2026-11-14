package dev.novux.dynasty.cbb.core.portrait

import dev.novux.dynasty.cbb.core.coach.CoachAvatarPresets
import dev.novux.dynasty.cbb.core.player.appearance.PlayerAvatarPresets

/** Pure preset → palette (repro across devices; no bitmap assets). */
data class PortraitRenderSpec(
    val presetId: String,
    val skinToneArgb: Long,
    val hairColorArgb: Long,
    val hairStyleId: String,
    val signatureHash: Int,
)

object PresetPortraitRenderer {
    private val skinArgb = mapOf(
        "skin_1" to 0xFFE8C4A0L,
        "skin_2" to 0xFFD4A574L,
        "skin_3" to 0xFFB8875AL,
        "skin_4" to 0xFF8D5524L,
        "skin_5" to 0xFF5C3A1FL,
    )

    private val hairArgb = mapOf(
        "hair_black" to 0xFF1A1410L,
        "hair_brown" to 0xFF4A3728L,
        "hair_blonde" to 0xFFD4B896L,
        "hair_red" to 0xFF8B4513L,
        "hair_gray" to 0xFF9E9E9EL,
    )

    fun specFor(avatarPresetId: String): PortraitRenderSpec {
        val (skin, hairColor, hairStyle) = when {
            avatarPresetId.startsWith("player-") -> PlayerAvatarPresets.parse(avatarPresetId)
            avatarPresetId.startsWith("coach-") -> parseCoachPreset(avatarPresetId)
            else -> Triple("skin_1", "hair_brown", "hair_short")
        }
        val skinArgbVal = skinArgb[skin] ?: skinArgb.getValue("skin_1")
        val hairArgbVal = hairArgb[hairColor] ?: hairArgb.getValue("hair_brown")
        val sig = (avatarPresetId.hashCode() xor skinArgbVal.toInt() xor hairArgbVal.toInt() xor hairStyle.hashCode())
        return PortraitRenderSpec(
            presetId = avatarPresetId,
            skinToneArgb = skinArgbVal,
            hairColorArgb = hairArgbVal,
            hairStyleId = hairStyle,
            signatureHash = sig,
        )
    }

    private fun parseCoachPreset(presetId: String): Triple<String, String, String> {
        val tokens = presetId.removePrefix("coach-").split("-").filter { it.isNotBlank() }
        val skin = tokens.firstOrNull { CoachAvatarPresets.isKnownSkin(it) } ?: "skin_1"
        val hairStyle = tokens.firstOrNull { CoachAvatarPresets.isKnownHairStyle(it) } ?: "hair_short"
        val hairColor = tokens.firstOrNull { CoachAvatarPresets.isKnownHairColor(it) } ?: "hair_brown"
        return Triple(skin, hairColor, hairStyle)
    }
}
