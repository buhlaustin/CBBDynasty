package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.balance.BalanceChemistry
import dev.novux.dynasty.cbb.core.chemistry.ChemistryClutchModifier

/** Team chemistry nudges clutch possessions ([MEGA-SHELL-077], [planning/in-season-systems.md#chemistry]). */
object SimChemistryClutchPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-077"

  private const val RATING_PER_WIN_PROB_POINT = 100.0

    fun offensiveRatingDelta(teamChemistry: Int, tuning: BalanceChemistry, clutchActive: Boolean): Int {
        if (!clutchActive) return 0
        val prob = ChemistryClutchModifier.winProbabilityDelta(teamChemistry, tuning)
        return (prob * RATING_PER_WIN_PROB_POINT).toInt()
    }

    fun extraTurnoverChance(teamChemistry: Int, clutchActive: Boolean): Double {
        if (!clutchActive || teamChemistry >= 50) return 0.0
        return 0.02
    }
}