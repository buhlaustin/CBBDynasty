package dev.novux.dynasty.cbb.core.player

import dev.novux.dynasty.cbb.core.player.archetype.PlayerArchetype
import dev.novux.dynasty.cbb.core.player.archetype.PlayerArchetypeAssigner
import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromise
import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromiseType
import dev.novux.dynasty.cbb.core.player.loyalty.LoyaltyBootstrap
import dev.novux.dynasty.cbb.core.player.loyalty.LoyaltyPersonalityTag
import dev.novux.dynasty.cbb.core.player.loyalty.LoyaltyRating
import dev.novux.dynasty.cbb.core.player.loyalty.PlayerLoyaltyProfile
import dev.novux.dynasty.cbb.core.player.loyalty.PromiseStatus
import dev.novux.dynasty.cbb.core.player.loyalty.TenurePromiseBootstrap
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.player.appearance.PlayerAppearance
import dev.novux.dynasty.cbb.core.roster.RosterPlayerSeed

object PlayerBootstrapFactory {
    fun enrichSeed(seed: RosterPlayerSeed): RosterPlayerSeed {
        val base = positionBaseRating(seed.position, seed.isStarter)
        val off = (base + positionOffBias(seed.position)).coerceIn(0, 99)
        val def = (base + positionDefBias(seed.position)).coerceIn(0, 99)
        val ratings = spreadRatings(base, off, def, seed.position)
        val archetype = PlayerArchetypeAssigner.assign(seed.position, ratings)
        val placeholder = placeholderIdentity(seed.playerId, seed.depthOrder)
        val (loyalty, personality) = LoyaltyBootstrap.seedLoyalty(seed.playerId, archetype.wire)
        val promise = seed.promiseTypeWire?.let { wire ->
            CoachPromiseType.fromWire(wire)?.let { type ->
                CoachPromise(
                    type = type,
                    stayTopN = seed.promiseStayTopN,
                    lockedAtSeasonIndex = seed.promiseLockedSeasonIndex,
                    status = PromiseStatus.fromWire(seed.promiseStatusWire) ?: PromiseStatus.Pending,
                )
            }
        } ?: TenurePromiseBootstrap.atHardCommit(seed.isStarter)
        val appearance = seed.resolvedAppearanceOrGenerate()
        return seed.copy(
            displayName = seed.displayName.ifBlank { placeholder.first },
            hometownCity = seed.hometownCity.ifBlank { placeholder.second },
            hometownState = seed.hometownState.ifBlank { placeholder.third },
            heightInches = defaultHeight(seed.position),
            weightLbs = defaultWeight(seed.position),
            ovr = ratings.ovr,
            off = ratings.off,
            def = ratings.def,
            shootingInside = ratings.shootingInside,
            shootingMid = ratings.shootingMid,
            shootingThree = ratings.shootingThree,
            handling = ratings.handling,
            passing = ratings.passing,
            rebounding = ratings.rebounding,
            perimeterDefense = ratings.perimeterDefense,
            interiorDefense = ratings.interiorDefense,
            stealing = ratings.stealing,
            blocking = ratings.blocking,
            potentialOverall = ratings.potentialOverall,
            proPotential = ratings.proPotential,
            archetypeWire = archetype.wire,
            eligibility = PlayerEligibility.forClassYear(seed.classYear),
            loyaltyRating = if (seed.loyaltyRating == 50 && seed.loyaltyPersonalityWire.isBlank()) {
                loyalty.value
            } else {
                seed.loyaltyRating
            },
            loyaltyPersonalityWire = seed.loyaltyPersonalityWire.ifBlank { personality.wire },
            promiseTypeWire = promise.type.wire,
            promiseStatusWire = promise.status.wire,
            promiseLockedSeasonIndex = promise.lockedAtSeasonIndex,
            promiseStayTopN = promise.stayTopN,
            skinToneId = appearance.skinToneId,
            hairColorId = appearance.hairColorId,
            hairStyleId = appearance.hairStyleId,
            avatarPresetId = appearance.avatarPresetId,
        )
    }

    private fun RosterPlayerSeed.resolvedAppearanceOrGenerate(): PlayerAppearance =
        if (avatarPresetId.isNotBlank()) {
            PlayerAppearance.fromPreset(avatarPresetId)
        } else if (skinToneId.isNotBlank()) {
            PlayerAppearance(skinToneId, hairColorId, hairStyleId)
        } else {
            PlayerAppearance.fromPlayerId(playerId)
        }

    fun fromSeed(seed: RosterPlayerSeed): DynastyPlayer {
        val enriched = if (seed.displayName.isBlank() || seed.promiseTypeWire == null) enrichSeed(seed) else seed
        val ratings = enriched.toRatings()
        val archetype = PlayerArchetype.fromWire(enriched.archetypeWire)
            ?: PlayerArchetypeAssigner.assign(enriched.position, ratings)
        val appearance = enriched.resolvedAppearanceOrGenerate()
        return DynastyPlayer(
            playerId = enriched.playerId,
            dynastyId = enriched.dynastyId,
            appearance = appearance,
            identity = PlayerIdentity(
                displayName = enriched.displayName,
                hometownCity = enriched.hometownCity,
                hometownState = enriched.hometownState,
                heightInches = enriched.heightInches,
                weightLbs = enriched.weightLbs,
            ),
            position = enriched.position,
            classYear = enriched.classYear,
            ratings = ratings,
            archetype = archetype,
            eligibility = enriched.eligibility,
            injury = PlayerInjuryState.Healthy,
            role = PlayerRosterRole(
                isStarter = enriched.isStarter,
                depthOrder = enriched.depthOrder,
                inTransferPortal = false,
                declaredForDraft = false,
                draftStockPoints = 0,
                proDayCompleted = false,
            ),
            loyalty = PlayerLoyaltyProfile(
                loyalty = LoyaltyRating(enriched.loyaltyRating),
                personality = LoyaltyPersonalityTag.fromWire(enriched.loyaltyPersonalityWire)
                    ?: LoyaltyPersonalityTag.ProgramGuy,
                promise = enriched.promiseTypeWire?.let { wire ->
                    CoachPromiseType.fromWire(wire)?.let { type ->
                        CoachPromise(
                            type = type,
                            stayTopN = enriched.promiseStayTopN,
                            lockedAtSeasonIndex = enriched.promiseLockedSeasonIndex,
                            status = PromiseStatus.fromWire(enriched.promiseStatusWire) ?: PromiseStatus.Pending,
                        )
                    }
                },
            ),
        )
    }

    private fun spreadRatings(ovr: Int, off: Int, def: Int, position: PlayerPosition): PlayerRatings =
        PlayerBootstrapFactorySpread.fromHeadline(ovr, off, def, position)

    private fun positionBaseRating(position: PlayerPosition, starter: Boolean): Int {
        val starterBump = if (starter) 8 else 0
        val pos = when (position) {
            PlayerPosition.PointGuard -> 68
            PlayerPosition.ShootingGuard -> 66
            PlayerPosition.SmallForward -> 65
            PlayerPosition.PowerForward -> 64
            PlayerPosition.Center -> 63
        }
        return (pos + starterBump).coerceIn(45, 92)
    }

    private fun positionOffBias(position: PlayerPosition): Int = when (position) {
        PlayerPosition.PointGuard, PlayerPosition.ShootingGuard -> 4
        PlayerPosition.Center -> -2
        else -> 0
    }

    private fun positionDefBias(position: PlayerPosition): Int = when (position) {
        PlayerPosition.Center, PlayerPosition.PowerForward -> 3
        PlayerPosition.ShootingGuard -> -2
        else -> 0
    }

    private fun defaultHeight(position: PlayerPosition): Int = when (position) {
        PlayerPosition.PointGuard -> 74
        PlayerPosition.ShootingGuard -> 76
        PlayerPosition.SmallForward -> 79
        PlayerPosition.PowerForward -> 80
        PlayerPosition.Center -> 82
    }

    private fun defaultWeight(position: PlayerPosition): Int = when (position) {
        PlayerPosition.PointGuard -> 185
        PlayerPosition.ShootingGuard -> 195
        PlayerPosition.SmallForward -> 210
        PlayerPosition.PowerForward -> 225
        PlayerPosition.Center -> 240
    }

    private fun placeholderIdentity(playerId: String, depth: Int): Triple<String, String, String> {
        val hash = playerId.hashCode().absoluteValue
        val first = FIRST_NAMES[hash % FIRST_NAMES.size]
        val last = LAST_NAMES[(hash / FIRST_NAMES.size) % LAST_NAMES.size]
        val city = CITIES[(hash + depth) % CITIES.size]
        val state = STATES[(hash + depth * 3) % STATES.size]
        return Triple("$first $last", city, state)
    }

    private val Int.absoluteValue: Int get() = if (this < 0) -this else this

    private val FIRST_NAMES = listOf("Jordan", "Miles", "Eli", "Cameron", "Devin", "Tyler", "Noah", "Marcus")
    private val LAST_NAMES = listOf("Hayes", "Brooks", "Carter", "Dixon", "Nguyen", "Walsh", "Okafor", "Silva")
    private val CITIES = listOf("Columbus", "Louisville", "Dallas", "Atlanta", "Indianapolis", "Omaha")
    private val STATES = listOf("OH", "KY", "TX", "GA", "IN", "NE")
}
