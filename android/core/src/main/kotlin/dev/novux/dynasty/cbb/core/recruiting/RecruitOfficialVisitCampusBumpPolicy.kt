package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActions

/** Official visits surface campus-life interest delta in the hub meter row ([MEGA-RECR-089]). */
object RecruitOfficialVisitCampusBumpPolicy {
    const val MEGA_ID: String = "MEGA-RECR-089"

    fun campusLifeInterestBump(
        actions: BalanceRecruitingActions,
        visitDistance: OfficialVisitDistanceBand,
    ): Int = RecruitInterestSpendDelta.forOfficialVisit(actions, visitDistance)

    fun showCampusBumpLine(officialVisitReserved: Boolean, interestDeltaFromVisit: Int): Boolean =
        officialVisitReserved && interestDeltaFromVisit > 0
}