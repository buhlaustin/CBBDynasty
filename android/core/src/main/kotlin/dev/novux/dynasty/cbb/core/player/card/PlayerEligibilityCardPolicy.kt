package dev.novux.dynasty.cbb.core.player.card

import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear

/** Overview eligibility copy — medical RS, development year, five-year path ([MEGA-GDD-023]). */
object PlayerEligibilityCardPolicy {
    const val MEGA_ID: String = "MEGA-GDD-023"

    enum class LineKind {
        MedicalRedshirt,
        DevelopmentYear,
        FiveYearPath,
    }

    fun overviewLineKinds(player: DynastyPlayer): List<LineKind> {
        val lines = mutableListOf<LineKind>()
        if (player.eligibility.medicalRedshirtSeason) {
            lines += LineKind.MedicalRedshirt
        }
        if (player.classYear != PlayerClassYear.WalkOn &&
            player.eligibility.yearsUsed < expectedYearsUsedForClass(player.classYear)
        ) {
            lines += LineKind.DevelopmentYear
        }
        if (player.eligibility.yearsUsed + player.eligibility.yearsRemaining > 4) {
            lines += LineKind.FiveYearPath
        }
        return lines
    }

    internal fun expectedYearsUsedForClass(classYear: PlayerClassYear): Int = when (classYear) {
        PlayerClassYear.Freshman, PlayerClassYear.WalkOn -> 0
        PlayerClassYear.Sophomore -> 1
        PlayerClassYear.Junior -> 2
        PlayerClassYear.Senior -> 3
    }
}