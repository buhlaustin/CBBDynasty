package dev.novux.dynasty.cbb.core.player.appearance

import dev.novux.dynasty.cbb.core.coach.AvatarPresetOption
import dev.novux.dynasty.cbb.core.coach.CoachAvatarPresets

/**
 * Player appearance catalog — same pack-safe ids as coach create ([planning/game-start.md]).
 */
object PlayerAvatarPresets {
    val skinTones: List<AvatarPresetOption> = CoachAvatarPresets.skinTones
    val hairColors: List<AvatarPresetOption> = CoachAvatarPresets.hairColors
    val hairStyles: List<AvatarPresetOption> = CoachAvatarPresets.hairStyles

    fun compositePresetId(skinToneId: String, hairColorId: String, hairStyleId: String): String =
        "player-$skinToneId-$hairColorId-$hairStyleId"

    fun isKnownPreset(id: String): Boolean {
        if (!id.startsWith("player-")) return false
        val (skin, hairColor, hairStyle) = parse(id)
        return CoachAvatarPresets.isKnownSkin(skin) &&
            CoachAvatarPresets.isKnownHairColor(hairColor) &&
            CoachAvatarPresets.isKnownHairStyle(hairStyle)
    }

    fun parse(presetId: String): Triple<String, String, String> {
        val tokens = presetId.removePrefix("player-").split("-").filter { it.isNotBlank() }
        val skin = tokens.firstOrNull { CoachAvatarPresets.isKnownSkin(it) } ?: skinTones.first().id
        val hairStyle = tokens.firstOrNull { CoachAvatarPresets.isKnownHairStyle(it) } ?: hairStyles.first().id
        val hairColor = tokens.firstOrNull { CoachAvatarPresets.isKnownHairColor(it) } ?: hairColors.first().id
        return Triple(skin, hairColor, hairStyle)
    }
}
