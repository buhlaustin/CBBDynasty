package dev.novux.dynasty.cbb.core.strategy

/** Extra stamina drain for taxing defensive presets while defending ([MEGA-SIM-147]). */
object SchemeFatigueCost {
    fun extraDefensiveDrainSeconds(scheme: DefensiveSchemePreset, tripSeconds: Int): Int {
        if (tripSeconds <= 0) return 0
        val fraction = when (scheme) {
            DefensiveSchemePreset.FullCourtPress -> 0.14
            DefensiveSchemePreset.BoxAndOne -> 0.07
            else -> 0.0
        }
        if (fraction <= 0.0) return 0
        return (tripSeconds * fraction).toInt().coerceAtLeast(1)
    }
}
