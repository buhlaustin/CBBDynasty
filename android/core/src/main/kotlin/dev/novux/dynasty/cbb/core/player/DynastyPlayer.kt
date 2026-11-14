package dev.novux.dynasty.cbb.core.player

import dev.novux.dynasty.cbb.core.player.archetype.PlayerArchetype
import dev.novux.dynasty.cbb.core.player.archetype.PlayerArchetypeAssigner
import dev.novux.dynasty.cbb.core.player.loyalty.PlayerLoyaltyProfile
import dev.novux.dynasty.cbb.core.player.scout.PlayerScoutReport
import dev.novux.dynasty.cbb.core.player.scout.ScoutDepth
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromise
import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromiseType
import dev.novux.dynasty.cbb.core.player.loyalty.LoyaltyPersonalityTag
import dev.novux.dynasty.cbb.core.player.loyalty.LoyaltyRating
import dev.novux.dynasty.cbb.core.player.loyalty.PlayerPromiseHistoryCodec
import dev.novux.dynasty.cbb.core.player.loyalty.PromiseStatus
import dev.novux.dynasty.cbb.core.player.appearance.PlayerAppearance
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** Full player record for cards and simulation — [planning/player-cards.md]. */
data class DynastyPlayer(
    val playerId: String,
    val dynastyId: String,
    val appearance: PlayerAppearance,
    val identity: PlayerIdentity,
    val position: PlayerPosition,
    val classYear: PlayerClassYear,
    val ratings: PlayerRatings,
    val archetype: PlayerArchetype,
    val eligibility: PlayerEligibility,
    val injury: PlayerInjuryState,
    val role: PlayerRosterRole,
    val loyalty: PlayerLoyaltyProfile = PlayerLoyaltyProfile.Empty,
    /** National Top 60 slot (1–60) when eligible; set at card open ([MEGA-COACH-163]). */
    val nationalTop60DraftRank: Int? = null,
) {
    fun scoutReport(depth: ScoutDepth = ScoutDepth.Light): PlayerScoutReport =
        PlayerScoutReport.fromPlayer(this, depth)

    fun toRosterPlayer(): RosterPlayer = RosterPlayer(
        playerId = playerId,
        dynastyId = dynastyId,
        position = position,
        classYear = classYear,
        isStarter = role.isStarter,
        depthOrder = role.depthOrder,
        inTransferPortal = role.inTransferPortal,
        declaredForDraft = role.declaredForDraft,
        draftStockPoints = role.draftStockPoints,
        proDayCompleted = role.proDayCompleted,
        displayName = identity.displayName,
        hometownCity = identity.hometownCity,
        hometownState = identity.hometownState,
        heightInches = identity.heightInches,
        weightLbs = identity.weightLbs,
        nilUsdPerYear = identity.nilUsdPerYear,
        nilDealRaisedThisSeason = identity.nilDealRaisedThisSeason,
        ovr = ratings.ovr,
        off = ratings.off,
        def = ratings.def,
        archetypeWire = archetype.wire,
        eligibilityYearsUsed = eligibility.yearsUsed,
        eligibilityYearsRemaining = eligibility.yearsRemaining,
        medicalRedshirtSeason = eligibility.medicalRedshirtSeason,
        injuryActive = injury.injuryActive,
        injuryId = injury.injuryId,
        injuryWeeksRemaining = injury.weeksRemaining,
        injuryLabel = injury.label,
        injurySeverityWire = injury.severityWire,
        injuryOccurredAtRsWeek = injury.occurredAtRsWeek,
        injuryOccurredAtRsGameIndex = injury.occurredAtRsGameIndex,
        rsGamesPlayedAtInjury = injury.rsGamesPlayedAtInjury,
        injuryWasStarterSlotWire = injury.wasStarterSlotWire,
        injuryPriorRotationBand = injury.priorRotationBand,
        starterSlotWire = role.starterSlotWire,
        rsGamesPlayedSeason = role.rsGamesPlayedSeason,
        loyaltyRating = loyalty.loyalty.value,
        loyaltyPersonalityWire = loyalty.personality.wire,
        promiseTypeWire = loyalty.promise?.type?.wire,
        promiseStatusWire = loyalty.promise?.status?.wire ?: "",
        promiseLockedSeasonIndex = loyalty.promise?.lockedAtSeasonIndex,
        promiseStayTopN = loyalty.promise?.stayTopN,
        promiseHistoryWire = PlayerPromiseHistoryCodec.encode(loyalty.promiseHistory),
        skinToneId = appearance.skinToneId,
        hairColorId = appearance.hairColorId,
        hairStyleId = appearance.hairStyleId,
        avatarPresetId = appearance.avatarPresetId,
    )

    companion object {
        fun fromRosterPlayer(player: RosterPlayer): DynastyPlayer = DynastyPlayer(
            playerId = player.playerId,
            dynastyId = player.dynastyId,
            appearance = player.resolvedAppearance(),
            identity = PlayerIdentity(
                displayName = player.displayName,
                hometownCity = player.hometownCity,
                hometownState = player.hometownState,
                heightInches = player.heightInches,
                weightLbs = player.weightLbs,
                nilUsdPerYear = player.nilUsdPerYear,
                nilDealRaisedThisSeason = player.nilDealRaisedThisSeason,
            ),
            position = player.position,
            classYear = player.classYear,
            ratings = player.toRatings(),
            archetype = PlayerArchetype.fromWire(player.archetypeWire)
                ?: PlayerArchetypeAssigner.assign(player.position, player.toRatings()),
            eligibility = PlayerEligibility(
                yearsUsed = player.eligibilityYearsUsed,
                yearsRemaining = player.eligibilityYearsRemaining,
                medicalRedshirtSeason = player.medicalRedshirtSeason,
            ),
            injury = PlayerInjuryState(
                injuryActive = player.injuryActive,
                injuryId = player.injuryId,
                weeksRemaining = player.injuryWeeksRemaining,
                label = player.injuryLabel,
                severityWire = player.injurySeverityWire,
                occurredAtRsWeek = player.injuryOccurredAtRsWeek,
                occurredAtRsGameIndex = player.injuryOccurredAtRsGameIndex,
                rsGamesPlayedAtInjury = player.rsGamesPlayedAtInjury,
                wasStarterSlotWire = player.injuryWasStarterSlotWire,
                priorRotationBand = player.injuryPriorRotationBand,
            ),
            role = PlayerRosterRole(
                isStarter = player.isStarter,
                depthOrder = player.depthOrder,
                inTransferPortal = player.inTransferPortal,
                declaredForDraft = player.declaredForDraft,
                draftStockPoints = player.draftStockPoints,
                proDayCompleted = player.proDayCompleted,
                starterSlotWire = player.starterSlotWire,
                rsGamesPlayedSeason = player.rsGamesPlayedSeason,
            ),
            loyalty = player.toLoyaltyProfile(),
        )
    }
}

private fun RosterPlayer.toLoyaltyProfile(): PlayerLoyaltyProfile {
    val personality = LoyaltyPersonalityTag.fromWire(loyaltyPersonalityWire)
        ?: LoyaltyPersonalityTag.ProgramGuy
    val promise = promiseTypeWire?.let { wire ->
        CoachPromiseType.fromWire(wire)?.let { type ->
            CoachPromise(
                type = type,
                stayTopN = promiseStayTopN,
                lockedAtSeasonIndex = promiseLockedSeasonIndex,
                status = PromiseStatus.fromWire(promiseStatusWire) ?: PromiseStatus.Pending,
            )
        }
    }
    val history = PlayerPromiseHistoryCodec.decode(promiseHistoryWire)
    return PlayerLoyaltyProfile(LoyaltyRating(loyaltyRating), personality, promise, history)
}
