package dev.novux.dynasty.cbb.core.strategy

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Full offensive×defensive interaction table — defaults plus optional tuning JSON overrides
 * ([planning/strategy-schemes.md], MEGA-SIM-146).
 */
object SchemeInteractionMatrix {
    val defaultMatrix: Map<Pair<OffensiveSchemePreset, DefensiveSchemePreset>, SchemeInteractionNudge> =
        buildDefaultMatrix()

    fun nudge(
        offense: OffensiveSchemePreset,
        defense: DefensiveSchemePreset,
        overrides: Map<Pair<OffensiveSchemePreset, DefensiveSchemePreset>, SchemeInteractionNudge> = emptyMap(),
    ): SchemeInteractionNudge =
        overrides[offense to defense] ?: defaultMatrix[offense to defense] ?: SchemeInteractionNudge()

    fun parseOverrides(raw: String): Map<Pair<OffensiveSchemePreset, DefensiveSchemePreset>, SchemeInteractionNudge> {
        val decoded = json.decodeFromString<SchemeInteractionMatrixWire>(raw)
        return decoded.cells.mapNotNull { cell ->
            val off = OffensiveSchemePreset.fromWire(cell.offense) ?: return@mapNotNull null
            val def = DefensiveSchemePreset.fromWire(cell.defense) ?: return@mapNotNull null
            val nudge = cell.toNudge()
            if (nudge == SchemeInteractionNudge()) return@mapNotNull null
            (off to def) to nudge
        }.toMap()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun buildDefaultMatrix(): Map<Pair<OffensiveSchemePreset, DefensiveSchemePreset>, SchemeInteractionNudge> {
        val matrix = mutableMapOf<Pair<OffensiveSchemePreset, DefensiveSchemePreset>, SchemeInteractionNudge>()
        OffensiveSchemePreset.entries.forEach { off ->
            DefensiveSchemePreset.entries.forEach { def ->
                matrix[off to def] = curatedNudge(off, def)
            }
        }
        return matrix
    }

    private fun curatedNudge(
        offense: OffensiveSchemePreset,
        defense: DefensiveSchemePreset,
    ): SchemeInteractionNudge = when (offense to defense) {
        OffensiveSchemePreset.Early to DefensiveSchemePreset.FullCourtPress ->
            SchemeInteractionNudge(turnoverAdjust = 0.03, tripPaceScale = 0.95)
        OffensiveSchemePreset.Perimeter to DefensiveSchemePreset.Zone23 ->
            SchemeInteractionNudge(threeAdjust = -0.02, makeAdjust = -0.02)
        OffensiveSchemePreset.Perimeter to DefensiveSchemePreset.Zone32 ->
            SchemeInteractionNudge(threeAdjust = 0.04, makeAdjust = -0.01)
        OffensiveSchemePreset.Post to DefensiveSchemePreset.Zone23 ->
            SchemeInteractionNudge(makeAdjust = -0.03)
        OffensiveSchemePreset.Drive to DefensiveSchemePreset.Zone23 ->
            SchemeInteractionNudge(makeAdjust = -0.02)
        OffensiveSchemePreset.Drive to DefensiveSchemePreset.SwitchingMan ->
            SchemeInteractionNudge(makeAdjust = -0.01, turnoverAdjust = 0.01)
        OffensiveSchemePreset.Motion to DefensiveSchemePreset.Zone131 ->
            SchemeInteractionNudge(turnoverAdjust = 0.02)
        OffensiveSchemePreset.Balanced to DefensiveSchemePreset.FullCourtPress ->
            SchemeInteractionNudge(turnoverAdjust = 0.015, tripPaceScale = 0.97)
        OffensiveSchemePreset.Motion to DefensiveSchemePreset.FullCourtPress ->
            SchemeInteractionNudge(turnoverAdjust = 0.02, tripPaceScale = 0.96)
        OffensiveSchemePreset.Set to DefensiveSchemePreset.Zone23 ->
            SchemeInteractionNudge(threeAdjust = -0.015, makeAdjust = -0.01)
        OffensiveSchemePreset.Post to DefensiveSchemePreset.BoxAndOne ->
            SchemeInteractionNudge(makeAdjust = -0.04, threeAdjust = 0.02)
        OffensiveSchemePreset.Perimeter to DefensiveSchemePreset.BoxAndOne ->
            SchemeInteractionNudge(threeAdjust = -0.03, makeAdjust = -0.02)
        OffensiveSchemePreset.Early to DefensiveSchemePreset.Zone131 ->
            SchemeInteractionNudge(turnoverAdjust = 0.025)
        OffensiveSchemePreset.Drive to DefensiveSchemePreset.FullCourtPress ->
            SchemeInteractionNudge(turnoverAdjust = 0.02, makeAdjust = 0.01)
        else -> baselineNudge(offense, defense)
    }

    private fun baselineNudge(
        offense: OffensiveSchemePreset,
        defense: DefensiveSchemePreset,
    ): SchemeInteractionNudge = when (defense) {
        DefensiveSchemePreset.FullCourtPress -> SchemeInteractionNudge(
            turnoverAdjust = 0.008,
            tripPaceScale = 0.99,
        )
        DefensiveSchemePreset.Zone23 -> when (offense) {
            OffensiveSchemePreset.Perimeter, OffensiveSchemePreset.Early ->
                SchemeInteractionNudge(threeAdjust = -0.01, makeAdjust = -0.01)
            OffensiveSchemePreset.Post, OffensiveSchemePreset.Drive ->
                SchemeInteractionNudge(makeAdjust = -0.015)
            else -> SchemeInteractionNudge(threeAdjust = -0.008)
        }
        DefensiveSchemePreset.Zone32 -> when (offense) {
            OffensiveSchemePreset.Perimeter, OffensiveSchemePreset.Early ->
                SchemeInteractionNudge(threeAdjust = 0.02)
            else -> SchemeInteractionNudge()
        }
        DefensiveSchemePreset.Zone131 -> SchemeInteractionNudge(turnoverAdjust = 0.006)
        DefensiveSchemePreset.BoxAndOne -> when (offense) {
            OffensiveSchemePreset.Post -> SchemeInteractionNudge(makeAdjust = -0.02)
            OffensiveSchemePreset.Perimeter -> SchemeInteractionNudge(threeAdjust = -0.015)
            else -> SchemeInteractionNudge()
        }
        DefensiveSchemePreset.SwitchingMan -> SchemeInteractionNudge(makeAdjust = -0.005)
        DefensiveSchemePreset.ManToMan -> SchemeInteractionNudge()
    }
}

@Serializable
private data class SchemeInteractionMatrixWire(
    val schemaVersion: Int = 1,
    val cells: List<SchemeInteractionCellWire> = emptyList(),
)

@Serializable
private data class SchemeInteractionCellWire(
    val offense: String,
    val defense: String,
    val threeAdjust: Double = 0.0,
    val turnoverAdjust: Double = 0.0,
    val makeAdjust: Double = 0.0,
    val tripPaceScale: Double = 1.0,
    val offRatingDelta: Int = 0,
    val defRatingDelta: Int = 0,
) {
    fun toNudge(): SchemeInteractionNudge = SchemeInteractionNudge(
        threeAdjust = threeAdjust,
        turnoverAdjust = turnoverAdjust,
        makeAdjust = makeAdjust,
        tripPaceScale = tripPaceScale,
        offRatingDelta = offRatingDelta,
        defRatingDelta = defRatingDelta,
    )
}
