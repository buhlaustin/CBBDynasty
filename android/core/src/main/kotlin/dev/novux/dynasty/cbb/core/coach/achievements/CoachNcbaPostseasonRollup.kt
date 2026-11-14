package dev.novux.dynasty.cbb.core.coach.achievements

/** NCBA postseason coach résumé for Overview ([MEGA-COACH-129]). */
data class CoachNcbaPostseasonRollup(
    val tourneyRecordLabel: String,
    val tourneyWinPercentLabel: String,
    val nationalTitles: Int,
    val finalFours: Int,
    val ncbaAppearances: Int,
) {
    companion object {
        fun from(achievements: HeadCoachAchievements): CoachNcbaPostseasonRollup =
            CoachNcbaPostseasonRollup(
                tourneyRecordLabel = achievements.ncbaTourneyRecordLabel,
                tourneyWinPercentLabel = achievements.ncbaTourneyWinPercentLabel,
                nationalTitles = achievements.nationalTitles,
                finalFours = achievements.finalFours,
                ncbaAppearances = achievements.ncbaAppearances,
            )
    }
}

object CoachNcbaPostseasonRollupPolicy {
    const val MEGA_ID: String = "MEGA-COACH-129"
}
