package dev.novux.dynasty.cbb.core.recruiting

/** Full HS/JUCO pool listings for the recruiting hub ([MEGA-GDD-027]). */
data class HsJucoBoard(
    val highSchool: List<RecruitProspect>,
    val juco: List<RecruitProspect>,
) {
    val totalCount: Int get() = highSchool.size + juco.size
}

object HsJucoRecruitBoard {
    fun partition(pool: List<RecruitProspect>): HsJucoBoard {
        val hs = pool
            .filter { it.poolKind == RecruitPoolKind.HighSchool }
            .sortedWith(
                compareBy<RecruitProspect> { it.nationalHsRank ?: Int.MAX_VALUE }
                    .thenByDescending { it.hiddenRating }
                    .thenBy { it.displayName },
            )
        val juco = pool
            .filter { it.poolKind == RecruitPoolKind.Juco }
            .sortedWith(
                compareByDescending<RecruitProspect> { it.hiddenRating }
                    .thenBy { it.displayName },
            )
        return HsJucoBoard(highSchool = hs, juco = juco)
    }
}
