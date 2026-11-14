package dev.novux.dynasty.cbb.core.roster

import dev.novux.dynasty.cbb.core.fatigue.FatigueEngine
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.PlayerRatings
import dev.novux.dynasty.cbb.core.player.RosterRatingSpread
import dev.novux.dynasty.cbb.core.player.appearance.PlayerAppearance
import dev.novux.dynasty.cbb.core.player.appearance.PlayerAvatarPresets

data class RosterPlayer(
    val playerId: String,
    val dynastyId: String,
    val position: PlayerPosition,
    val classYear: PlayerClassYear,
    val isStarter: Boolean,
    val depthOrder: Int,
    val inTransferPortal: Boolean,
    val declaredForDraft: Boolean = false,
    val draftStockPoints: Int = 0,
    val proDayCompleted: Boolean = false,
    val displayName: String = "",
    val hometownCity: String = "",
    val hometownState: String = "",
    val heightInches: Int = 76,
    val weightLbs: Int = 200,
    val nilUsdPerYear: Long = 0,
    val nilDealRaisedThisSeason: Boolean = false,
    val ovr: Int = 60,
    val off: Int = 60,
    val def: Int = 60,
    val eligibilityYearsUsed: Int = 0,
    val eligibilityYearsRemaining: Int = 4,
    val medicalRedshirtSeason: Boolean = false,
    val injuryActive: Boolean = false,
    val injuryId: String? = null,
    val injuryWeeksRemaining: Int = 0,
    val injuryLabel: String? = null,
    val injurySeverityWire: String? = null,
    val injuryOccurredAtRsWeek: Int = 0,
    val injuryOccurredAtRsGameIndex: Int = 0,
    val rsGamesPlayedAtInjury: Int = 0,
    val rsGamesPlayedSeason: Int = 0,
    val injuryWasStarterSlotWire: String? = null,
    val injuryPriorRotationBand: Int? = null,
    val starterSlotWire: String? = null,
    val gamesSuspendedRemaining: Int = 0,
    val academicHold: Boolean = false,
    val gpaCurrent: Double = 2.5,
    val academicCoverUp: Boolean = false,
    val conductIncidentId: String? = null,
    val archetypeWire: String = "",
    val loyaltyRating: Int = 50,
    val loyaltyPersonalityWire: String = "",
    val promiseTypeWire: String? = null,
    val promiseStatusWire: String = "",
    val promiseLockedSeasonIndex: Int? = null,
    val promiseStayTopN: Int? = null,
    val promiseHistoryWire: String = "",
    val stamina: Int = FatigueEngine.FRESH,
    val skinToneId: String = "",
    val hairColorId: String = "",
    val hairStyleId: String = "",
    val avatarPresetId: String = "",
) {
    fun resolvedAppearance(): PlayerAppearance = when {
        avatarPresetId.isNotBlank() && PlayerAvatarPresets.isKnownPreset(avatarPresetId) ->
            PlayerAppearance.fromPreset(avatarPresetId)
        skinToneId.isNotBlank() ->
            PlayerAppearance(
                skinToneId = skinToneId,
                hairColorId = hairColorId.ifBlank { PlayerAvatarPresets.hairColors.first().id },
                hairStyleId = hairStyleId.ifBlank { PlayerAvatarPresets.hairStyles.first().id },
            )
        else -> PlayerAppearance.fromPlayerId(playerId)
    }

    fun displayLabel(): String =
        if (displayName.isNotBlank()) displayName else "${position.label} · ${classYear.label}"

    fun toRatings(): PlayerRatings = RosterRatingSpread.fromHeadline(ovr, off, def, position)

    fun toDynastyPlayer(): DynastyPlayer = DynastyPlayer.fromRosterPlayer(this)
}
