package dev.novux.dynasty.cbb.core.player.appearance

import kotlin.random.Random

/** Deterministic player portrait preset until roster rows persist `avatarPresetId`. */
object PlayerAppearanceResolver {
    fun presetIdForPlayer(playerId: String): String {
        val r = Random(playerId.hashCode().toLong())
        val skin = PlayerAvatarPresets.skinTones[r.nextInt(PlayerAvatarPresets.skinTones.size)].id
        val hairColor = PlayerAvatarPresets.hairColors[r.nextInt(PlayerAvatarPresets.hairColors.size)].id
        val hairStyle = PlayerAvatarPresets.hairStyles[r.nextInt(PlayerAvatarPresets.hairStyles.size)].id
        return PlayerAvatarPresets.compositePresetId(skin, hairColor, hairStyle)
    }
}
