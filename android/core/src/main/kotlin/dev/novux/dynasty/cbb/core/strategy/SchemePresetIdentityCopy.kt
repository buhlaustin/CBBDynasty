package dev.novux.dynasty.cbb.core.strategy

/** One-line identity blurbs from [planning/strategy-schemes.md] (MEGA-SIM-152). */
object SchemePresetIdentityCopy {
    fun offensive(preset: OffensiveSchemePreset): String = when (preset) {
        OffensiveSchemePreset.Balanced -> "Neutral shot mix, average pace"
        OffensiveSchemePreset.Motion -> "Cuts and screens; lower isolation, moderate pace"
        OffensiveSchemePreset.Drive -> "Rim pressure, foul draw, kick-outs"
        OffensiveSchemePreset.Set -> "Quick-hitters and half-court sets"
        OffensiveSchemePreset.Early -> "Transition push; higher pace"
        OffensiveSchemePreset.Perimeter -> "High 3PA rate, spacing, drive & kick"
        OffensiveSchemePreset.Post -> "Interior touches; PF/C usage bump"
    }

    fun defensive(preset: DefensiveSchemePreset): String = when (preset) {
        DefensiveSchemePreset.ManToMan -> "Standard matchup defense"
        DefensiveSchemePreset.Zone23 -> "Pack the paint; contest threes from wings"
        DefensiveSchemePreset.Zone32 -> "Pressure the perimeter; softer interior"
        DefensiveSchemePreset.Zone131 -> "Trap edges; gamble passing lanes"
        DefensiveSchemePreset.BoxAndOne -> "Four on star; one chases primary scorer"
        DefensiveSchemePreset.FullCourtPress -> "Turnover chase with fatigue cost"
        DefensiveSchemePreset.SwitchingMan -> "Switch all ball screens; perimeter versatile"
    }
}
