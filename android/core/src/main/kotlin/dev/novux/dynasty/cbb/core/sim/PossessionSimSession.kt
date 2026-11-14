package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.injuries.MidGameInjurySampler
import dev.novux.dynasty.cbb.core.strategy.GameplanEffectsResolver
import dev.novux.dynasty.cbb.core.strategy.LateGameFoulStrategy
import dev.novux.dynasty.cbb.core.strategy.LateGamePossessionPolicy
import dev.novux.dynasty.cbb.core.strategy.GameplanUsageNormalizer
import dev.novux.dynasty.cbb.core.sim.tuning.SimEventWeightsResolver
import dev.novux.dynasty.cbb.core.strategy.SchemeFatigueCost
import dev.novux.dynasty.cbb.core.strategy.SchemeMatchupResolver
import dev.novux.dynasty.cbb.core.strategy.SchemeOffensiveSlotUsageBias
import kotlin.random.Random

data class PossessionStep(
    val logLine: String,
    val haltedAtHalftime: Boolean,
    val chrome: GameWatchChrome,
)

/**
 * Steppable possession loop for watch playback and halftime resume.
 */
class PossessionSimSession(
    input: GameSimInput,
    resume: MidGameState? = null,
    private val stopAtHalftime: Boolean = false,
    private val injuryRoll: GameSimInjuryRollInput? = null,
) {
    private val input: GameSimInput = input
    private var random: Random = resume?.random ?: Random(input.seed)
    private var gameClock: Int = resume?.gameClockSeconds ?: GameSimClock.REGULATION_SECONDS
    private var shotClockSeconds: Int = resume?.shotClockSecondsRemaining
        ?: PossessionShotClock.resetOnChangeOfPossession(input.tuning.shotClock)
    private var offenseHome: Boolean = resume?.offenseHome ?: random.nextBoolean()
    private var homeBox: TeamBoxScore = resume?.homeBox ?: TeamBoxScore(teamId = input.home.teamId)
    private var awayBox: TeamBoxScore = resume?.awayBox ?: TeamBoxScore(teamId = input.away.teamId)
    private val log: MutableList<String> = resume?.possessionLog?.toMutableList() ?: mutableListOf()
    private var totalPossessions: Int = resume?.totalPossessions ?: 0
    private val homeFoulLedger = PersonalFoulLedger(resume?.homePersonalFoulsByPlayer.orEmpty())
    private val awayFoulLedger = PersonalFoulLedger(resume?.awayPersonalFoulsByPlayer.orEmpty())
    private val homeMinutesLedger = SimFloorMinutesLedger(resume?.homeFloorSecondsByPlayer.orEmpty())
    private val awayMinutesLedger = SimFloorMinutesLedger(resume?.awayFloorSecondsByPlayer.orEmpty())
    private val homePlayerLedger = PlayerBoxScoreLedger(
        playerIds = input.homeRoster?.participants?.map { it.playerId }.orEmpty(),
        initial = resume?.homePlayerBox.orEmpty(),
    )
    private val awayPlayerLedger = PlayerBoxScoreLedger(
        playerIds = input.awayRoster?.participants?.map { it.playerId }.orEmpty(),
        initial = resume?.awayPlayerBox.orEmpty(),
    )
    private val homeInjuredOut = (resume?.homeInjuredPlayerIds ?: emptySet()).toMutableSet()
    private val awayInjuredOut = (resume?.awayInjuredPlayerIds ?: emptySet()).toMutableSet()
    private val homeDisqualifiedOut = (
        resume?.homeDisqualifiedPlayerIds
            ?: PersonalFoulDisqualificationPolicy.disqualifiedIdsFromSnapshot(
                resume?.homePersonalFoulsByPlayer.orEmpty(),
            )
        ).toMutableSet()
    private val awayDisqualifiedOut = (
        resume?.awayDisqualifiedPlayerIds
            ?: PersonalFoulDisqualificationPolicy.disqualifiedIdsFromSnapshot(
                resume?.awayPersonalFoulsByPlayer.orEmpty(),
            )
        ).toMutableSet()
    private val homeMidInjuries = (resume?.homeMidGameInjuries ?: emptyList()).toMutableList()
    private val awayMidInjuries = (resume?.awayMidGameInjuries ?: emptyList()).toMutableList()
    private var inOvertime: Boolean = resume?.inOvertime ?: false
    private var overtimePeriods: Int = resume?.overtimePeriods ?: 0
    private val teamFoulLedger = TeamFoulGameLedger(resume = resume)
    private val eventWeights = SimEventWeightsResolver.resolve(input.tuning.eventWeights)
    private val homeStaminaLedger = SimStaminaLedger(
        roster = input.homeRoster,
        initial = resume?.homeStaminaByPlayer.orEmpty(),
    )
    private val awayStaminaLedger = SimStaminaLedger(
        roster = input.awayRoster,
        initial = resume?.awayStaminaByPlayer.orEmpty(),
    )

    fun hasNext(): Boolean = gameClock > 0

    fun isTiedAtBuzz(): Boolean =
        gameClock <= 0 && homeBox.points == awayBox.points

    fun beginOvertimePeriod() {
        check(isTiedAtBuzz()) { "Cannot start OT unless regulation/OT period ended tied" }
        overtimePeriods += 1
        inOvertime = true
        gameClock = GameSimClock.OVERTIME_SECONDS
        shotClockSeconds = PossessionShotClock.resetOnChangeOfPossession(input.tuning.shotClock)
        offenseHome = random.nextBoolean()
        teamFoulLedger.onOvertimePeriodStart()
        log += "— OT$overtimePeriods (5:00) — ${homeBox.points}-${awayBox.points}"
    }

    fun periodsPlayedTotal(): Int = 1 + overtimePeriods

    fun latestLogLine(): String = log.last()

    fun currentWatchChrome(shotClockSeconds: Int = this.shotClockSeconds): GameWatchChrome =
        GameWatchChromeFactory.create(
            gameClockSeconds = gameClock,
            shotClockSeconds = shotClockSeconds,
            inOvertime = inOvertime,
            overtimePeriods = overtimePeriods,
        )

    fun step(): PossessionStep {
        check(hasNext()) { "No possessions remaining" }
        val sides = if (offenseHome) {
            PossessionSimEngine.Sides(input.home, input.away, homeBox, "HOME")
        } else {
            PossessionSimEngine.Sides(input.away, input.home, awayBox, "AWAY")
        }
        val schemeEffects = SchemeMatchupResolver.resolve(
            sides.offense.offensiveScheme,
            sides.defense.defensiveScheme,
        )
        val offGameplan = if (offenseHome) {
            input.activeHomeGameplan(gameClock)
        } else {
            input.activeAwayGameplan(gameClock)
        }
        val defGameplan = if (offenseHome) {
            input.activeAwayGameplan(gameClock)
        } else {
            input.activeHomeGameplan(gameClock)
        }
        val defenseScore = if (offenseHome) awayBox.points else homeBox.points
        val offenseScore = if (offenseHome) homeBox.points else awayBox.points
        val usagePeak = GameplanUsageNormalizer.normalize(offGameplan?.usageWeights.orEmpty())
            .values
            .maxOrNull() ?: 0.2
        var effects = GameplanEffectsResolver.mergeInto(
            schemeEffects,
            GameplanEffectsResolver.offenseTweaks(offGameplan, usagePeak),
            GameplanEffectsResolver.defenseTweaks(defGameplan, gameClock, defenseScore, offenseScore),
        )
        val offenseSituation = ScoreDifferentialSituationPolicy.classify(
            gameClock,
            offenseScore,
            defenseScore,
            eventWeights.situation,
        )
        effects = ScoreDifferentialSituationPolicy.applyToEffects(
            effects,
            offenseSituation,
            eventWeights.situation,
        )
        val lateDecision = LateGamePossessionPolicy.defenseDecision(
            strategy = defGameplan?.lateGameFoulStrategy ?: LateGameFoulStrategy.Standard,
            gameClockSeconds = gameClock,
            defenseScore = defenseScore,
            offenseScore = offenseScore,
        )
        val tripSample = PossessionSimEngine.sampleTripForLateGame(
            input.tuning,
            random,
            lateDecision,
            eventWeights,
        )
        val rawTripSeconds = if (ProtectLeadLateGamePolicy.isActive(lateDecision)) {
            ProtectLeadLateGamePolicy.adjustRawTripSeconds(tripSample.rawSeconds, eventWeights.protectLead)
        } else {
            tripSample.rawSeconds
        }
        val paceGovernor = PossessionPaceGovernor.tripPaceMultiplier(
            totalPossessionsSoFar = totalPossessions,
            gameClockRemaining = gameClock,
            tuning = input.tuning,
        )
        val tripSeconds = PossessionSimEngine.pacedTripSecondsFromRaw(
            input.tuning,
            rawTripSeconds,
            effects.tripPaceScale * paceGovernor,
        )
        val shotClockBefore = shotClockSeconds
        val clockBeforeTrip = gameClock
        val used = PossessionShotClock.secondsConsumed(tripSeconds, shotClockSeconds, gameClock)
        gameClock -= used
        if (crossedRegulationHalfBoundary(clockBeforeTrip, gameClock)) {
            teamFoulLedger.onRegulationHalfBoundaryCrossed()
            val sealed = teamFoulLedger.summary()
            log += "— Halftime team fouls — HOME ${sealed.homeFirstHalf} AWAY ${sealed.awayFirstHalf}"
        }
        val shotClockAfterTrip = PossessionShotClock.remainingAfterTrip(shotClockBefore, used)
        totalPossessions++

        val lateClockTrip = PossessionLateClockPolicy.isLateClockTrip(
            used,
            input.tuning.shotClock.full,
        )
        val foulEarlyStopClock = FoulEarlyLateGamePolicy.isFoulToStopClock(tripSample)
        var foulStoppage = tripSample.foulStoppage || foulEarlyStopClock
        if (lateClockTrip && !foulStoppage && random.nextDouble() < PossessionLateClockPolicy.EXTRA_FOUL_STOPPAGE_RATE) {
            foulStoppage = true
        }
        val defenseSituationFoulNudge = ScoreDifferentialSituationPolicy.trailingDefenseFoulNudge(
            gameClockSeconds = clockBeforeTrip,
            defenseScore = defenseScore,
            offenseScore = offenseScore,
            weights = eventWeights.situation,
        )
        if (
            !foulStoppage &&
            defenseSituationFoulNudge > 0.0 &&
            random.nextDouble() < defenseSituationFoulNudge
        ) {
            foulStoppage = true
        }
        val homeFloorIdsEarly = floorPlayerIds(home = true)
        val awayFloorIdsEarly = floorPlayerIds(home = false)
        val offenseFloorEarly = fallbackFloor(
            if (offenseHome) homeFloorIdsEarly else awayFloorIdsEarly,
            if (offenseHome) input.homeRoster else input.awayRoster,
            if (offenseHome) homeFoulLedger else awayFoulLedger,
            if (offenseHome) homeDisqualifiedOut else awayDisqualifiedOut,
        )
        val offenseRosterForShooting = if (offenseHome) input.homeRoster else input.awayRoster
        val resolveEffects = FloorShootingEffects.merge(
            if (lateClockTrip) PossessionLateClockPolicy.applyToEffects(effects) else effects,
            participantsOnFloor(offenseFloorEarly, offenseRosterForShooting),
        )
        val defenseFloorEarly = fallbackFloor(
            if (offenseHome) awayFloorIdsEarly else homeFloorIdsEarly,
            if (offenseHome) input.awayRoster else input.homeRoster,
            if (offenseHome) awayFoulLedger else homeFoulLedger,
            if (offenseHome) awayDisqualifiedOut else homeDisqualifiedOut,
        )
        val denyPrimaryCtx = DenyPrimaryScorerPolicy.context(
            defGameplan = defGameplan,
            offenseRoster = if (offenseHome) input.homeRoster else input.awayRoster,
            offenseFloor = offenseFloorEarly,
            defenseFloor = defenseFloorEarly,
            defenseRoster = if (offenseHome) input.awayRoster else input.homeRoster,
        )
        val denyTurnoverBoost = DenyPrimaryScorerPolicy.extraTurnoverChance(denyPrimaryCtx, eventWeights.denyPrimary)
        var resolveEffectsForPossession = if (denyTurnoverBoost > 0.0) {
            resolveEffects.copy(
                turnoverChance = (resolveEffects.turnoverChance + denyTurnoverBoost).coerceIn(0.10, 0.40),
            )
        } else {
            resolveEffects
        }
        val clutchActiveForChemistry = ClutchUsagePolicy.isClutch(
            gameClock,
            offenseScore,
            defenseScore,
            eventWeights.clutchUsage,
        )
        val offenseChemistry = if (offenseHome) input.homeTeamChemistry else input.awayTeamChemistry
        val chemistryTuning = input.chemistryTuning
        if (clutchActiveForChemistry && offenseChemistry != null && chemistryTuning != null) {
            val chemTurnover = SimChemistryClutchPolicy.extraTurnoverChance(offenseChemistry, true)
            if (chemTurnover > 0.0) {
                resolveEffectsForPossession = resolveEffectsForPossession.copy(
                    turnoverChance = (resolveEffectsForPossession.turnoverChance + chemTurnover).coerceIn(0.10, 0.45),
                )
            }
        }
        val venueMods = input.tuning.homeNeutralModifiers
        val venueHome = input.homeVenue
        var offRating = teamOffensiveRating(offenseIsHome = offenseHome) + effects.offensiveRatingDelta
        var defRating = teamDefensiveRating(defenseIsHome = !offenseHome) + effects.defensiveRatingDelta
        if (clutchActiveForChemistry && offenseChemistry != null && chemistryTuning != null) {
            offRating += SimChemistryClutchPolicy.offensiveRatingDelta(offenseChemistry, chemistryTuning, true)
        }
        offRating = SimVenueRatingPolicy.offensiveRating(offRating, offenseHome, venueHome, venueMods) +
            SimVenueRatingPolicy.lateGameHomeCrowdOffenseBoost(offenseHome, gameClock, venueHome)
        defRating = SimVenueRatingPolicy.defensiveRating(defRating, !offenseHome, venueHome, venueMods)
        val resolution = when {
            foulEarlyStopClock ->
                IntentionalFoulLateGamePolicy.resolvePossession(sides.box)
            PossessionShotClock.isShotClockViolation(shotClockBefore, used, tripSeconds) ->
                PossessionSimEngine.resolveShotClockViolation(sides.box)
            else ->
                PossessionSimEngine.resolvePossessionForSession(
                    sides.box,
                    offRating,
                    defRating,
                    resolveEffectsForPossession,
                    random,
                    lateClockTrip = lateClockTrip,
                    eventWeights = eventWeights,
                    protectLeadDefense = ProtectLeadLateGamePolicy.isActive(lateDecision),
                )
        }
        val turnoversAdded = resolution.box.turnovers - sides.box.turnovers
        var offenseBoxAfter = resolution.box
        var subPhaseChain = resolution.subPhaseChain
        if (resolution.freeThrowAttempts > 0) {
            val (makes, attempts) = PlayerPossessionStatRecorder.sampleFreeThrowChain(
                resolution.freeThrowAttempts,
                random,
            )
            offenseBoxAfter = offenseBoxAfter.copy(
                points = offenseBoxAfter.points + makes,
                freeThrowsMade = offenseBoxAfter.freeThrowsMade + makes,
                freeThrowsAttempted = offenseBoxAfter.freeThrowsAttempted + attempts,
            )
            subPhaseChain = listOfNotNull(
                subPhaseChain,
                "FT $makes/$attempts",
            ).joinToString(" | ")
        }
        val offenseRosterPresent = if (offenseHome) input.homeRoster != null else input.awayRoster != null
        var defensiveFoulEvent: DefensiveFoulEvent? = null
        var teamBonusLogTag: String? = null
        if (
            offenseRosterPresent &&
            PlayerPossessionStatRecorder.shouldShootBonusFreeThrows(sides.box, offenseBoxAfter, foulStoppage)
        ) {
            defensiveFoulEvent = recordDefensivePersonalFoul(offenseHome)
            val foulsInHalf = defensiveFoulEvent?.teamFoulsInHalf
            if (foulsInHalf != null) {
                val bonusFt = TeamFoulBonusPolicy.sampleNonShootingFreeThrows(
                    foulsInHalf,
                    input.tuning.teamFoulRules,
                    random,
                )
                if (bonusFt != null) {
                    val (makes, attempts) = bonusFt
                    offenseBoxAfter = offenseBoxAfter.copy(
                        points = offenseBoxAfter.points + makes,
                        freeThrowsMade = offenseBoxAfter.freeThrowsMade + makes,
                        freeThrowsAttempted = offenseBoxAfter.freeThrowsAttempted + attempts,
                    )
                    teamBonusLogTag = TeamFoulBonusPolicy.logTag(
                        TeamFoulBonusPolicy.stateAfterFoul(foulsInHalf, input.tuning.teamFoulRules),
                    )
                    subPhaseChain = listOfNotNull(
                        subPhaseChain,
                        teamBonusLogTag,
                        "team foul FT $makes/$attempts",
                    ).joinToString(" | ")
                }
            }
        }
        offenseBoxAfter = PossessionPeriodEndPolicy.applyScoringRules(
            before = sides.box,
            after = offenseBoxAfter,
            rules = eventWeights.periodEnd,
            clockBeforeTrip = clockBeforeTrip,
            clockAfterTrip = gameClock,
            inOvertime = inOvertime,
        )
        val periodEndCtx = PossessionPeriodEndPolicy.context(
            before = sides.box,
            after = offenseBoxAfter,
            clockBeforeTrip = clockBeforeTrip,
            clockAfterTrip = gameClock,
            inOvertime = inOvertime,
        )
        val periodEndLogTag = PossessionPeriodEndPolicy.logTag(periodEndCtx)
        val homeFloorIds = homeFloorIdsEarly
        val awayFloorIds = awayFloorIdsEarly
        val offenseFloor = fallbackFloor(
            if (offenseHome) homeFloorIds else awayFloorIds,
            if (offenseHome) input.homeRoster else input.awayRoster,
            if (offenseHome) homeFoulLedger else awayFoulLedger,
            if (offenseHome) homeDisqualifiedOut else awayDisqualifiedOut,
        )
        val defenseFloor = fallbackFloor(
            if (offenseHome) awayFloorIds else homeFloorIds,
            if (offenseHome) input.awayRoster else input.homeRoster,
            if (offenseHome) awayFoulLedger else homeFoulLedger,
            if (offenseHome) awayDisqualifiedOut else homeDisqualifiedOut,
        )
        val offenseLedger = if (offenseHome) homePlayerLedger else awayPlayerLedger
        val defenseLedger = if (offenseHome) awayPlayerLedger else homePlayerLedger
        val baseUsage = offGameplan?.usageWeights.orEmpty()
        val offensiveScheme = if (offenseHome) input.home.offensiveScheme else input.away.offensiveScheme
        val offenseRosterForUsage = if (offenseHome) input.homeRoster else input.awayRoster
        val schemeBiasedUsage = SchemeOffensiveSlotUsageBias.apply(
            scheme = offensiveScheme,
            roster = offenseRosterForUsage,
            offenseFloor = offenseFloor,
            baseWeights = baseUsage,
        )
        val clutchActive = ClutchUsagePolicy.isClutch(
            gameClock,
            offenseScore,
            defenseScore,
            eventWeights.clutchUsage,
        )
        val usageWeights = ClutchUsagePolicy.effectiveUsageWeights(
            baseWeights = schemeBiasedUsage,
            offenseFloor = offenseFloor,
            gameClockSeconds = gameClock,
            offenseScore = offenseScore,
            defenseScore = defenseScore,
            tuning = eventWeights.clutchUsage,
        )
        val clutchPeakId = ClutchUsagePolicy.peakUsagePlayerId(
            if (baseUsage.isEmpty()) offenseFloor.associateWith { 20 } else baseUsage,
        )
        val playerCredits = PlayerPossessionStatRecorder.record(
            offenseLedger = offenseLedger,
            defenseLedger = defenseLedger,
            offenseFloor = offenseFloor,
            defenseFloor = defenseFloor,
            usageWeights = usageWeights,
            before = sides.box,
            after = offenseBoxAfter,
            change = resolution.change,
            turnoverTerminal = resolution.turnoverTerminal,
            foulStoppage = foulStoppage || resolution.foulOnBlock,
            blockedShot = resolution.blockedShot,
            random = random,
        )
        val offenseRoster = if (offenseHome) input.homeRoster else input.awayRoster
        val defenseRoster = if (offenseHome) input.awayRoster else input.homeRoster
        val playerAttribution = PossessionLogPlayerLabel.format(
            credits = playerCredits,
            offenseRoster = offenseRoster,
            defenseRoster = defenseRoster,
            before = sides.box,
            after = offenseBoxAfter,
            change = resolution.change,
            blockedShot = resolution.blockedShot,
        )
        if (offenseHome) {
            homeBox = offenseBoxAfter
        } else {
            awayBox = offenseBoxAfter
        }
        val shotClockAfterLog = when (resolution.change) {
            PossessionChange.DefenseTakesBall -> {
                offenseHome = !offenseHome
                shotClockSeconds = PossessionShotClock.resetOnChangeOfPossession(input.tuning.shotClock)
                shotClockSeconds
            }
            PossessionChange.OffenseRetainsOffensiveRebound -> {
                shotClockSeconds = PossessionShotClock.resetAfterOffensiveRebound(input.tuning.shotClock)
                shotClockSeconds
            }
        }
        accrueFloorMinutes(used, offenseHome)
        val newInjuryLines = sampleMidGameInjuries(homeFloorIds, awayFloorIds)
        val needsDefensiveFoulLog =
            resolution.foulOnBlock || resolution.andOne || resolution.shootingFoul || foulStoppage
        if (needsDefensiveFoulLog && defensiveFoulEvent == null) {
            defensiveFoulEvent = recordDefensivePersonalFoul(offenseHome)
        }
        var denyPrimaryFoul = false
        if (
            defensiveFoulEvent == null &&
            DenyPrimaryScorerPolicy.rollDenyFoulOnPrimaryTouch(
                denyPrimaryCtx,
                playerCredits,
                turnoversAdded,
                random,
                eventWeights.denyPrimary,
            )
        ) {
            defensiveFoulEvent = recordDefensivePersonalFoul(
                offenseHome,
                defendingPlayerId = denyPrimaryCtx.assignedDefenderId,
            )
            denyPrimaryFoul = defensiveFoulEvent != null
        }
        val pfLog = defensiveFoulEvent?.personalFoulLog
        val foulNote = when {
            denyPrimaryFoul ->
                DenyPrimaryScorerPolicy.foulLogLabel(denyPrimaryCtx, pfLog)
            foulEarlyStopClock -> FoulEarlyLateGamePolicy.foulLogLabel(tripSample, pfLog)
            resolution.foulOnBlock -> pfLog?.let { "foul on block | $it" } ?: "foul on block"
            resolution.andOne -> pfLog?.let { "and-1 | $it" } ?: "and-1"
            resolution.shootingFoul -> pfLog?.let { "shooting foul | $it" } ?: "shooting foul"
            foulStoppage -> pfLog
            else -> null
        }
        val lateTags = listOfNotNull(
            ScoreDifferentialSituationPolicy.logTag(offenseSituation),
            if (ProtectLeadLateGamePolicy.isActive(lateDecision)) ProtectLeadLateGamePolicy.logTag() else null,
            FoulEarlyLateGamePolicy.logTag(tripSample),
            LateGamePossessionPolicy.logTag(lateDecision),
            ClutchUsagePolicy.logTag(clutchActive, clutchPeakId),
            DenyPrimaryScorerPolicy.logTag(denyPrimaryCtx),
            if (lateClockTrip) "late-clock" else null,
            teamBonusLogTag,
            periodEndLogTag,
        ).joinToString("; ").takeIf { it.isNotEmpty() }
        val line = PossessionSimEngine.formatPossessionLog(
            used,
            gameClock,
            sides.label,
            sides.box,
            resolution.box,
            shotClockBefore = shotClockBefore,
            shotClockAfter = shotClockAfterLog,
            change = resolution.change,
            turnoverTerminal = resolution.turnoverTerminal,
            foulNote = foulNote,
            lateGameTag = lateTags,
            subPhaseChain = subPhaseChain,
            blockedShot = resolution.blockedShot,
            foulOnBlock = resolution.foulOnBlock,
            shootingFoul = resolution.shootingFoul,
            playerAttribution = playerAttribution,
            intentionalFoul = foulEarlyStopClock,
        )
        log += line
        newInjuryLines.forEach { log += it }
        val halftime = stopAtHalftime && !inOvertime && gameClock <= GameSimClock.HALF_SECONDS
        val chrome = GameWatchChromeFactory.create(
            gameClockSeconds = gameClock,
            shotClockSeconds = shotClockAfterLog,
            inOvertime = inOvertime,
            overtimePeriods = overtimePeriods,
        )
        return PossessionStep(line, haltedAtHalftime = halftime, chrome = chrome)
    }

    private fun sampleMidGameInjuries(
        homeFloorIds: List<String>,
        awayFloorIds: List<String>,
    ): List<String> {
        val roll = injuryRoll ?: return emptyList()
        val lines = mutableListOf<String>()
        roll.homeRosterPlayers?.let { roster ->
            MidGameInjurySampler.tryTeamPossession(
                phase = roll.phase,
                floorPlayerIds = homeFloorIds.filter { it !in homeInjuredOut },
                minutesLedger = homeMinutesLedger,
                roster = roster,
                alreadyInjuredIds = homeInjuredOut,
                injuriesThisTeam = homeMidInjuries.size,
                bank = roll.bank,
                tuning = roll.occurrence,
                medicalStaffGrade = roll.medicalStaffGrade,
                gameClockSeconds = gameClock,
                possessionIndex = totalPossessions,
                teamSeed = input.seed xor input.home.teamId.hashCode().toLong(),
                random = random,
            )?.let { event ->
                homeMidInjuries += event
                homeInjuredOut += event.playerId
                val label = roster.firstOrNull { it.playerId == event.playerId }?.displayLabel()
                    ?: event.playerId
                lines += MidGameInjurySampler.formatLogLine(label, event.label)
            }
        }
        roll.awayRosterPlayers?.let { roster ->
            MidGameInjurySampler.tryTeamPossession(
                phase = roll.phase,
                floorPlayerIds = awayFloorIds.filter { it !in awayInjuredOut },
                minutesLedger = awayMinutesLedger,
                roster = roster,
                alreadyInjuredIds = awayInjuredOut,
                injuriesThisTeam = awayMidInjuries.size,
                bank = roll.bank,
                tuning = roll.occurrence,
                medicalStaffGrade = roll.medicalStaffGrade,
                gameClockSeconds = gameClock,
                possessionIndex = totalPossessions,
                teamSeed = input.seed xor input.away.teamId.hashCode().toLong(),
                random = random,
            )?.let { event ->
                awayMidInjuries += event
                awayInjuredOut += event.playerId
                val label = roster.firstOrNull { it.playerId == event.playerId }?.displayLabel()
                    ?: event.playerId
                lines += MidGameInjurySampler.formatLogLine(label, event.label)
            }
        }
        return lines
    }

    private fun teamOffensiveRating(offenseIsHome: Boolean): Int {
        val roster = if (offenseIsHome) input.homeRoster else input.awayRoster
        val foulLedger = if (offenseIsHome) homeFoulLedger else awayFoulLedger
        val minutesLedger = if (offenseIsHome) homeMinutesLedger else awayMinutesLedger
        val staminaLedger = if (offenseIsHome) homeStaminaLedger else awayStaminaLedger
        return roster?.weightedOffensiveOnFloor(
            foulLedger,
            minutesLedger,
            gameClock,
            staminaLedger,
            input.fatigueTuning,
            input.tuning.positionComfort,
        )
            ?: if (offenseIsHome) input.homeOffensive() else input.awayOffensive()
    }

    private fun teamDefensiveRating(defenseIsHome: Boolean): Int {
        val roster = if (defenseIsHome) input.homeRoster else input.awayRoster
        val foulLedger = if (defenseIsHome) homeFoulLedger else awayFoulLedger
        val minutesLedger = if (defenseIsHome) homeMinutesLedger else awayMinutesLedger
        val staminaLedger = if (defenseIsHome) homeStaminaLedger else awayStaminaLedger
        return roster?.weightedDefensiveOnFloor(
            foulLedger,
            minutesLedger,
            gameClock,
            staminaLedger,
            input.fatigueTuning,
            input.tuning.positionComfort,
        )
            ?: if (defenseIsHome) input.homeDefensive() else input.awayDefensive()
    }

    private fun participantsOnFloor(
        playerIds: List<String>,
        roster: GameSimRosterInput?,
    ): List<GameSimParticipant> {
        if (roster == null) return emptyList()
        val byId = roster.participants.associateBy { it.playerId }
        return playerIds.mapNotNull { byId[it] }
    }

    private fun fallbackFloor(
        ids: List<String>,
        roster: GameSimRosterInput?,
        foulLedger: PersonalFoulLedger,
        disqualifiedOut: Set<String>,
    ): List<String> {
        val eligible = PersonalFoulDisqualificationPolicy.filterEligibleOnFloor(
            ids,
            foulLedger,
            disqualifiedOut,
        )
        return eligible.ifEmpty {
            PersonalFoulDisqualificationPolicy.filterRosterFallback(roster, foulLedger, disqualifiedOut)
        }
    }

    private fun floorPlayerIds(home: Boolean, excludeInjured: Boolean = true): List<String> {
        val roster = if (home) input.homeRoster else input.awayRoster
        if (roster == null) return emptyList()
        val foulLedger = if (home) homeFoulLedger else awayFoulLedger
        val minutesLedger = if (home) homeMinutesLedger else awayMinutesLedger
        val injured = if (home) homeInjuredOut else awayInjuredOut
        val staminaLedger = if (home) homeStaminaLedger else awayStaminaLedger
        val disqualified = if (home) homeDisqualifiedOut else awayDisqualifiedOut
        return PersonalFoulDisqualificationPolicy.filterEligibleOnFloor(
            SimFloorRotationPolicy.floorParticipants(
                roster,
                foulLedger,
                minutesLedger,
                gameClock,
                staminaLedger,
                input.fatigueTuning,
            )
                .map { it.playerId }
                .filter { !excludeInjured || it !in injured },
            foulLedger,
            disqualified,
        )
    }

    private fun accrueFloorMinutes(usedSeconds: Int, offenseIsHome: Boolean) {
        val tuning = input.fatigueTuning
        val homeFloor = floorPlayerIds(home = true)
        val awayFloor = floorPlayerIds(home = false)
        if (input.homeRoster != null) {
            homeMinutesLedger.addFloorTime(homeFloor, usedSeconds)
            homeStaminaLedger.drainFloorTime(homeFloor, usedSeconds, tuning)
        }
        if (input.awayRoster != null) {
            awayMinutesLedger.addFloorTime(awayFloor, usedSeconds)
            awayStaminaLedger.drainFloorTime(awayFloor, usedSeconds, tuning)
        }
        val defendingHome = !offenseIsHome
        val defendingScheme = if (defendingHome) input.home.defensiveScheme else input.away.defensiveScheme
        val extraDrain = SchemeFatigueCost.extraDefensiveDrainSeconds(defendingScheme, usedSeconds)
        if (extraDrain > 0) {
            val defenders = if (defendingHome) homeFloor else awayFloor
            val ledger = if (defendingHome) homeStaminaLedger else awayStaminaLedger
            ledger.drainFloorTime(defenders, extraDrain, tuning)
        }
    }

    private data class DefensiveFoulEvent(
        val personalFoulLog: String,
        val teamFoulsInHalf: Int,
    )

    private fun crossedRegulationHalfBoundary(before: Int, after: Int): Boolean =
        !inOvertime && before > GameSimClock.HALF_SECONDS && after <= GameSimClock.HALF_SECONDS

    private fun recordDefensivePersonalFoul(
        offenseIsHome: Boolean,
        defendingPlayerId: String? = null,
    ): DefensiveFoulEvent? {
        val defendingRoster = (if (offenseIsHome) input.awayRoster else input.homeRoster) ?: return null
        val ledger = if (offenseIsHome) awayFoulLedger else homeFoulLedger
        val playerId = defendingPlayerId?.takeIf { id ->
            defendingRoster.participants.any { it.playerId == id }
        } ?: PersonalFoulAssignment.pickPlayer(defendingRoster, ledger, random) ?: return null
        val record = ledger.record(playerId)
        val disqualifiedOut = if (offenseIsHome) awayDisqualifiedOut else homeDisqualifiedOut
        PersonalFoulDisqualificationPolicy.registerIfDisqualified(record, disqualifiedOut)
        if (offenseIsHome) {
            awayBox = awayBox.copy(personalFouls = awayBox.personalFouls + 1)
        } else {
            homeBox = homeBox.copy(personalFouls = homeBox.personalFouls + 1)
        }
        val teamFouls = teamFoulLedger.recordDefensiveFoul(defenseIsHome = !offenseIsHome)
        if (record.disqualified) {
            val teamLabel = if (offenseIsHome) "AWAY" else "HOME"
            log += PersonalFoulDisqualificationPolicy.dqBannerLine(teamLabel, record)
        }
        val pfLog = if (record.disqualified) {
            "PF $playerId (${record.foulCount}) DQ"
        } else {
            "PF $playerId (${record.foulCount})"
        }
        return DefensiveFoulEvent(personalFoulLog = pfLog, teamFoulsInHalf = teamFouls)
    }

    fun toMidGame(): MidGameState {
        val teamFoulFields = teamFoulLedger.restoreMidGameFields()
        return MidGameState(
        random = random,
        gameClockSeconds = gameClock,
        offenseHome = offenseHome,
        homeBox = homeBox,
        awayBox = awayBox,
        possessionLog = log.toList(),
        totalPossessions = totalPossessions,
        shotClockSecondsRemaining = shotClockSeconds,
        homePersonalFoulsByPlayer = homeFoulLedger.snapshot(),
        awayPersonalFoulsByPlayer = awayFoulLedger.snapshot(),
        homeFloorSecondsByPlayer = homeMinutesLedger.snapshot(),
        awayFloorSecondsByPlayer = awayMinutesLedger.snapshot(),
        homePlayerBox = homePlayerLedger.snapshotMap(),
        awayPlayerBox = awayPlayerLedger.snapshotMap(),
        homeInjuredPlayerIds = homeInjuredOut.toSet(),
        awayInjuredPlayerIds = awayInjuredOut.toSet(),
        homeDisqualifiedPlayerIds = homeDisqualifiedOut.toSet(),
        awayDisqualifiedPlayerIds = awayDisqualifiedOut.toSet(),
        homeMidGameInjuries = homeMidInjuries.toList(),
        awayMidGameInjuries = awayMidInjuries.toList(),
        inOvertime = inOvertime,
        overtimePeriods = overtimePeriods,
        homeTeamFoulsInHalf = teamFoulFields.homeTeamFoulsInHalf,
        awayTeamFoulsInHalf = teamFoulFields.awayTeamFoulsInHalf,
        homeTeamFoulsFirstHalf = teamFoulFields.homeTeamFoulsFirstHalf,
        awayTeamFoulsFirstHalf = teamFoulFields.awayTeamFoulsFirstHalf,
        homeStaminaByPlayer = homeStaminaLedger.snapshot(),
        awayStaminaByPlayer = awayStaminaLedger.snapshot(),
    )
    }

    fun toResult(isComplete: Boolean): GameSimResult {
        syncPlayerBoxAuxiliaryStats(isComplete = isComplete)
        return GameSimResult(
        homeScore = homeBox.points,
        awayScore = awayBox.points,
        homeBox = homeBox,
        awayBox = awayBox,
        totalPossessions = totalPossessions,
        possessionLog = log.toList(),
        isComplete = isComplete,
        midGame = if (isComplete) null else toMidGame(),
        homePersonalFoulsByPlayer = homeFoulLedger.snapshot(),
        awayPersonalFoulsByPlayer = awayFoulLedger.snapshot(),
        homeFloorSecondsByPlayer = homeMinutesLedger.snapshot(),
        awayFloorSecondsByPlayer = awayMinutesLedger.snapshot(),
        homePlayerBox = homePlayerLedger.snapshot(),
        awayPlayerBox = awayPlayerLedger.snapshot(),
        homeInjuries = homeMidInjuries.toList(),
        awayInjuries = awayMidInjuries.toList(),
        periodsPlayed = periodsPlayedTotal(),
        overtimePeriods = overtimePeriods,
        teamFoulsByHalf = teamFoulLedger.summary(),
        homeDisqualifiedPlayerIds = homeDisqualifiedOut.toSet(),
        awayDisqualifiedPlayerIds = awayDisqualifiedOut.toSet(),
        )
    }

    private fun syncPlayerBoxAuxiliaryStats(isComplete: Boolean) {
        homeFoulLedger.snapshot().forEach { (id, pf) -> homePlayerLedger.setPersonalFouls(id, pf) }
        awayFoulLedger.snapshot().forEach { (id, pf) -> awayPlayerLedger.setPersonalFouls(id, pf) }
        homeMinutesLedger.snapshot().forEach { (id, sec) -> homePlayerLedger.setMinutesSeconds(id, sec) }
        awayMinutesLedger.snapshot().forEach { (id, sec) -> awayPlayerLedger.setMinutesSeconds(id, sec) }
        if (isComplete) {
            val expectedSeconds = TeamPlayerMinutesCoherence.expectedTeamFloorSeconds(overtimePeriods)
            TeamPlayerMinutesCoherence.reconcileFloorMinutes(homePlayerLedger, expectedSeconds)
            TeamPlayerMinutesCoherence.reconcileFloorMinutes(awayPlayerLedger, expectedSeconds)
        }
        PlayerBoxReconciler.reconcile(homeBox, homePlayerLedger)
        PlayerBoxReconciler.reconcile(awayBox, awayPlayerLedger)
    }
}
