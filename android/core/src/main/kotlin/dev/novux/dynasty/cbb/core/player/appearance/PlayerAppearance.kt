package dev.novux.dynasty.cbb.core.player.appearance

import kotlinx.serialization.Serializable

/** Pack-safe appearance components on roster rows — [planning/game-start.md#appearance-preset-avatars]. */
@Serializable
data class PlayerAppearance(
    val skinToneId: String,
    val hairColorId: String,
    val hairStyleId: String,
) {
    val avatarPresetId: String
        get() = PlayerAvatarPresets.compositePresetId(skinToneId, hairColorId, hairStyleId)

    companion object {
        fun fromPlayerId(playerId: String): PlayerAppearance {
            val preset = PlayerAppearanceResolver.presetIdForPlayer(playerId)
            val (skin, hairColor, hairStyle) = PlayerAvatarPresets.parse(preset)
            return PlayerAppearance(skin, hairColor, hairStyle)
        }

        fun fromPreset(presetId: String): PlayerAppearance {
            val (skin, hairColor, hairStyle) = PlayerAvatarPresets.parse(presetId)
            return PlayerAppearance(skin, hairColor, hairStyle)
        }
    }
}
