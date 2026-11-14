package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActions
import kotlin.math.roundToInt

/** Maps balance action costs to interest meter movement ([planning/recruiting-engine.md#spend--interest]). */
object RecruitInterestSpendDelta {
    fun forPointAction(action: RecruitingPointAction, actions: BalanceRecruitingActions, bonusAmount: Int?): Int =
        when (action) {
            RecruitingPointAction.Dm -> actions.dm.interestDelta
            RecruitingPointAction.PhoneCall -> actions.phoneCall.interestDelta
            RecruitingPointAction.SendTheHouse -> actions.sendTheHouse.interestDelta
            RecruitingPointAction.BonusPoints -> {
                val amount = bonusAmount ?: 0
                if (amount <= 0) 0
                else {
                    val perPoint = actions.bonusPoints.interestDeltaPerPoint.coerceAtLeast(0)
                    if (perPoint == 0) 0
                    else (amount.toDouble() / perPoint).roundToInt().coerceAtLeast(1)
                }
            }
            RecruitingPointAction.ScholarshipOffer -> actions.scholarshipOffer.interestDelta
            RecruitingPointAction.ScoutTheirGame,
            RecruitingPointAction.OfficialVisit,
            -> 0
        }

    fun forOfficialVisit(actions: BalanceRecruitingActions, band: OfficialVisitDistanceBand): Int =
        when (band) {
            OfficialVisitDistanceBand.Local -> actions.officialVisit.costByDistanceBand.interestDeltaLocal
            OfficialVisitDistanceBand.Regional -> actions.officialVisit.costByDistanceBand.interestDeltaRegional
            OfficialVisitDistanceBand.Remote -> actions.officialVisit.costByDistanceBand.interestDeltaRemote
        }
}
