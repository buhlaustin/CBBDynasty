package dev.novux.dynasty.cbb.core.roster

import dev.novux.dynasty.cbb.core.fatigue.FatigueEngine
import dev.novux.dynasty.cbb.core.player.PlayerEligibility
import dev.novux.dynasty.cbb.core.player.PlayerRatings

/**
 * Generated roster row at dynasty create — enriched to [dev.novux.dynasty.cbb.core.player.DynastyPlayer].
 */
data class RosterPlayerSeed(
    val playerId: String,
    val dynastyId: String,
    val position: PlayerPosition,
    val classYear: PlayerClassYear,
    val isStarter: Boolean,
    val depthOrder: Int,
    val displayName: String = "",
    val hometownCity: String = "",
    val hometownState: String = "",
    val heightInches: Int = 76,
    val weightLbs: Int = 200,
    val ovr: Int = 60,
    val off: Int = 60,
    val def: Int = 60,
    val shootingInside: Int = ovr,
    val shootingMid: Int = ovr,
    val shootingThree: Int = ovr,
    val handling: Int = ovr,
    val passing: Int = ovr,
    val rebounding: Int = ovr,
    val perimeterDefense: Int = ovr,
    val interiorDefense: Int = ovr,
    val stealing: Int = ovr,
    val blocking: Int = ovr,
    val potentialOverall: Int = ovr,
    val proPotential: Int = ovr,
    val archetypeWire: String = "",
    val eligibility: PlayerEligibility = PlayerEligibility.forClassYear(classYear),
    val loyaltyRating: Int = 50,
    val loyaltyPersonalityWire: String = "",
    val promiseTypeWire: String? = null,
    val promiseStatusWire: String = "",
    val promiseLockedSeasonIndex: Int? = null,
    val promiseStayTopN: Int? = null,
    val stamina: Int = FatigueEngine.FRESH,
    val skinToneId: String = "",
    val hairColorId: String = "",
    val hairStyleId: String = "",
    val avatarPresetId: String = "",
) {
    fun toRatings(): PlayerRatings = PlayerRatings(
        ovr = ovr,
        off = off,
        def = def,
        shootingInside = shootingInside,
        shootingMid = shootingMid,
        shootingThree = shootingThree,
        handling = handling,
        passing = passing,
        rebounding = rebounding,
        perimeterDefense = perimeterDefense,
        interiorDefense = interiorDefense,
        stealing = stealing,
        blocking = blocking,
        potentialOverall = potentialOverall,
        proPotential = proPotential,
    )
}
