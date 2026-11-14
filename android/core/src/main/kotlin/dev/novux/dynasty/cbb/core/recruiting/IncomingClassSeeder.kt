package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.preseason.IncomingClassSeedRunPhase
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import kotlin.random.Random

/**
 * Preseason HS + JUCO pool generation ([planning/recruiting-engine.md#timeline--seeding]).
 */
object IncomingClassSeeder {
    const val HS_POOL_MIN: Int = 2_000
    const val HS_POOL_MAX: Int = 4_000
    const val JUCO_POOL_SIZE: Int = 1_500
    const val TOP_NATIONAL_RANKS: Int = 60

    /** Deterministic uniform draw in [HS_POOL_MIN, HS_POOL_MAX] per enrolling class ([planning/recruiting-engine.md]). */
    fun resolveHsPoolSize(enrollingSeasonIndex: Int, seedKey: Long): Int {
        val random = Random(seedKey xor (enrollingSeasonIndex.toLong() * 982_451_653))
        return HS_POOL_MIN + random.nextInt(HS_POOL_MAX - HS_POOL_MIN + 1)
    }

    private val firstNames = listOf(
        "Jordan", "Eli", "Marcus", "Tyler", "Cole", "Darius", "Liam", "Noah", "Jayden", "Kai",
    )
    private val lastNames = listOf(
        "Walker", "Brooks", "Hayes", "Carter", "Mills", "Foster", "Reed", "Shaw", "Parks", "Lang",
    )
    private val states = listOf("TX", "OH", "FL", "CA", "NC", "IN", "KY", "VA", "IL", "GA", "MI", "PA")

    fun seed(
        enrollingSeasonIndex: Int,
        hsCount: Int? = null,
        jucoCount: Int = JUCO_POOL_SIZE,
        seedKey: Long = enrollingSeasonIndex.toLong(),
        onPhase: ((IncomingClassSeedRunPhase) -> Unit)? = null,
    ): List<RecruitProspect> {
        val resolvedHsCount = hsCount ?: resolveHsPoolSize(enrollingSeasonIndex, seedKey)
        val random = Random(seedKey xor (enrollingSeasonIndex.toLong() * 524_287))
        onPhase?.invoke(IncomingClassSeedRunPhase.RollingHighSchoolPool)
        val hs = generatePool(
            enrollingSeasonIndex,
            resolvedHsCount,
            RecruitPoolKind.HighSchool,
            "hs",
            random,
            jucoBias = false,
        )
        onPhase?.invoke(IncomingClassSeedRunPhase.RollingJucoPool)
        val juco = generatePool(
            enrollingSeasonIndex,
            jucoCount,
            RecruitPoolKind.Juco,
            "juco",
            random,
            jucoBias = true,
        )
        val combined = hs + juco
        onPhase?.invoke(IncomingClassSeedRunPhase.RankingNationalRecruits)
        val ranked = combined.sortedByDescending { it.hiddenRating }
        val rankById = ranked.take(TOP_NATIONAL_RANKS)
            .mapIndexed { index, prospect -> prospect.prospectId to (index + 1) }
            .toMap()
        return combined.map { prospect ->
            prospect.copy(nationalHsRank = rankById[prospect.prospectId])
        }
    }

    private fun generatePool(
        seasonIndex: Int,
        count: Int,
        kind: RecruitPoolKind,
        idPrefix: String,
        random: Random,
        jucoBias: Boolean,
    ): List<RecruitProspect> {
        val size = count.coerceAtLeast(1)
        val hiddenByCreationOrder = (0 until count)
            .map { rank -> RecruitHiddenRatingSampler.forPercentileRank(rank, size, random) }
            .shuffled(random)
        val raw = List(count) { index ->
            val hidden = hiddenByCreationOrder[index]
            val position = PlayerPosition.entries[random.nextInt(PlayerPosition.entries.size)]
            val state = states[random.nextInt(states.size)]
            val prospectId = "$idPrefix-$seasonIndex-$index"
            RecruitProspect(
                prospectId = prospectId,
                displayName = "${firstNames[random.nextInt(firstNames.size)]} " +
                    "${lastNames[random.nextInt(lastNames.size)]}",
                stars = 2,
                position = position,
                homeState = state,
                nilAskUsd = 0,
                scoutLetterOverall = "C",
                topPriorities = RecruitPriorityRoll.roll(prospectId),
                poolKind = kind,
                hiddenRating = hidden,
            )
        }
        return RecruitStarAssignmentEngine.assignStarsWithinPool(raw, jucoBias)
    }

    /** Percentile bands per pool ([planning/recruiting-engine.md#star-distribution]). */
    internal fun starBandForPercentile(percentile: Double): Int = when {
        percentile < 0.02 -> 5
        percentile < 0.20 -> 4
        percentile < 0.50 -> 3
        else -> 2
    }

    internal fun nilAskForStars(stars: Int, jucoBias: Boolean): Int {
        val factor = if (jucoBias) 0.85 else 1.0
        val base = when (stars) {
            5 -> 450_000
            4 -> 180_000
            3 -> 60_000
            else -> 15_000
        }
        return (base * factor).toInt()
    }

}
