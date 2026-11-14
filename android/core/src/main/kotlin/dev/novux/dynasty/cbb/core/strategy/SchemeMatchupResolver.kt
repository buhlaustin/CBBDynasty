package dev.novux.dynasty.cbb.core.strategy

/**
 * Merges offensive preset, defensive preset, and light 7×7 interaction nudges for one possession.
 */
object SchemeMatchupResolver {
    private val tuningOverrides: Map<Pair<OffensiveSchemePreset, DefensiveSchemePreset>, SchemeInteractionNudge> by lazy {
        runCatching {
            val stream = SchemeInteractionMatrix::class.java.classLoader
                ?.getResourceAsStream("sim/scheme-interaction-matrix.json")
                ?: return@lazy emptyMap()
            SchemeInteractionMatrix.parseOverrides(stream.bufferedReader().readText())
        }.getOrElse { emptyMap() }
    }

    fun resolve(
        offense: OffensiveSchemePreset,
        defense: DefensiveSchemePreset,
    ): ResolvedSchemeEffects {
        val off = SchemeModifierCatalog.offensive(offense)
        val def = SchemeModifierCatalog.defensive(defense)
        val interaction = SchemeInteractionMatrix.nudge(offense, defense, tuningOverrides)
        val threeRate = (off.threePointAttemptRate + def.threePointAttemptAdjust + interaction.threeAdjust)
            .coerceIn(0.12, 0.62)
        val turnover = (off.turnoverChance + def.turnoverPressure + interaction.turnoverAdjust)
            .coerceIn(0.10, 0.40)
        val makeAdjust = off.makeRateBonus - def.makeRatePenalty + interaction.makeAdjust
        val pace = (off.tripPaceScale * interaction.tripPaceScale).coerceIn(0.82, 1.18)
        return ResolvedSchemeEffects(
            tripPaceScale = pace,
            threePointAttemptRate = threeRate,
            turnoverChance = turnover,
            makeRateAdjust = makeAdjust,
            offensiveRatingDelta = off.ratingDelta + interaction.offRatingDelta,
            defensiveRatingDelta = def.ratingDelta + interaction.defRatingDelta,
        )
    }
}
