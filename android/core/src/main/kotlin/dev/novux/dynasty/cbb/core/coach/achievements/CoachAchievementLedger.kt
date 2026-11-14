package dev.novux.dynasty.cbb.core.coach.achievements

import kotlinx.serialization.Serializable

/** Cumulative head-coach achievements for one dynasty save ([MEGA-COACH-036]). */
@Serializable
data class CoachAchievementLedger(
    val dynastyId: String,
    val rolledSeasons: Set<Int> = emptySet(),
    val careerWins: Int = 0,
    val careerLosses: Int = 0,
    val ncbaTourneyWins: Int = 0,
    val ncbaTourneyLosses: Int = 0,
    val nationalTitles: Int = 0,
    val finalFours: Int = 0,
    val ncbaAppearances: Int = 0,
    val confRegTitles: Int = 0,
    val confTourneyTitles: Int = 0,
    val nciWins: Int = 0,
    val nciLosses: Int = 0,
    val nciTitles: Int = 0,
    val heritageWins: Int = 0,
    val heritageLosses: Int = 0,
    val heritageTitles: Int = 0,
) {
    operator fun plus(delta: CoachAchievementLedger): CoachAchievementLedger = CoachAchievementLedger(
        dynastyId = dynastyId,
        rolledSeasons = rolledSeasons + delta.rolledSeasons,
        careerWins = careerWins + delta.careerWins,
        careerLosses = careerLosses + delta.careerLosses,
        ncbaTourneyWins = ncbaTourneyWins + delta.ncbaTourneyWins,
        ncbaTourneyLosses = ncbaTourneyLosses + delta.ncbaTourneyLosses,
        nationalTitles = nationalTitles + delta.nationalTitles,
        finalFours = finalFours + delta.finalFours,
        ncbaAppearances = ncbaAppearances + delta.ncbaAppearances,
        confRegTitles = confRegTitles + delta.confRegTitles,
        confTourneyTitles = confTourneyTitles + delta.confTourneyTitles,
        nciWins = nciWins + delta.nciWins,
        nciLosses = nciLosses + delta.nciLosses,
        nciTitles = nciTitles + delta.nciTitles,
        heritageWins = heritageWins + delta.heritageWins,
        heritageLosses = heritageLosses + delta.heritageLosses,
        heritageTitles = heritageTitles + delta.heritageTitles,
    )
}

@Serializable
data class HeadCoachAchievements(
    val recordLabel: String,
    val winPercentLabel: String,
    val ncbaTourneyRecordLabel: String,
    val ncbaTourneyWinPercentLabel: String,
    val nationalTitles: Int,
    val finalFours: Int,
    val ncbaAppearances: Int,
    val confRegTitles: Int,
    val confTourneyTitles: Int,
    val nciRecordLabel: String,
    val nciTitles: Int,
    val heritageRecordLabel: String,
    val heritageTitles: Int,
) {
    companion object {
        fun fromLedger(ledger: CoachAchievementLedger): HeadCoachAchievements = HeadCoachAchievements(
            recordLabel = formatRecord(ledger.careerWins, ledger.careerLosses),
            winPercentLabel = formatWinPct(ledger.careerWins, ledger.careerLosses),
            ncbaTourneyRecordLabel = formatRecord(ledger.ncbaTourneyWins, ledger.ncbaTourneyLosses),
            ncbaTourneyWinPercentLabel = formatWinPct(ledger.ncbaTourneyWins, ledger.ncbaTourneyLosses),
            nationalTitles = ledger.nationalTitles,
            finalFours = ledger.finalFours,
            ncbaAppearances = ledger.ncbaAppearances,
            confRegTitles = ledger.confRegTitles,
            confTourneyTitles = ledger.confTourneyTitles,
            nciRecordLabel = formatRecord(ledger.nciWins, ledger.nciLosses),
            nciTitles = ledger.nciTitles,
            heritageRecordLabel = formatRecord(ledger.heritageWins, ledger.heritageLosses),
            heritageTitles = ledger.heritageTitles,
        )

        private fun formatRecord(wins: Int, losses: Int): String = "$wins-$losses"

        private fun formatWinPct(wins: Int, losses: Int): String {
            val games = wins + losses
            if (games == 0) return "—"
            val pct = wins * 1000 / games
            return "${pct / 10}.${pct % 10}%"
        }

        /** Fallback when ledger snapshot is unavailable — uses current season W–L label. */
        fun fromSeasonRecordLabel(seasonRecordLabel: String): HeadCoachAchievements {
            val (wins, losses) = parseSeasonRecord(seasonRecordLabel)
            return fromLedger(
                CoachAchievementLedger(
                    dynastyId = "",
                    careerWins = wins,
                    careerLosses = losses,
                ),
            )
        }

        fun parseSeasonRecord(seasonRecordLabel: String): Pair<Int, Int> {
            val parts = seasonRecordLabel.split("-", limit = 2)
            if (parts.size != 2) return 0 to 0
            val wins = parts[0].trim().toIntOrNull()?.coerceAtLeast(0) ?: 0
            val losses = parts[1].trim().toIntOrNull()?.coerceAtLeast(0) ?: 0
            return wins to losses
        }
    }
}
