package dev.novux.dynasty.cbb.core.coach

/** Deterministic wizard-style preset ids for coach-track staff without persisted appearance rows. */
object CoachTrackPortraitIds {
    fun forStaffId(staffId: String): String {
        val h = staffId.hashCode()
        val skin = CoachAvatarPresets.skinTones[(h and 0x7) % CoachAvatarPresets.skinTones.size].id
        val hairColor = CoachAvatarPresets.hairColors[(h shr 3 and 0x7) % CoachAvatarPresets.hairColors.size].id
        val hairStyle = CoachAvatarPresets.hairStyles[(h shr 6 and 0x7) % CoachAvatarPresets.hairStyles.size].id
        val beard = CoachAvatarPresets.beardStyles[(h shr 9 and 0x7) % CoachAvatarPresets.beardStyles.size].id
        return "coach-$skin-$hairColor-$hairStyle-$beard"
    }
}
