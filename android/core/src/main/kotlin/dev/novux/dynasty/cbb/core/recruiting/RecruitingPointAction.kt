package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActions
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActionEntry

enum class RecruitingPointAction {
    ScholarshipOffer,
    Dm,
    PhoneCall,
    SendTheHouse,
    ScoutTheirGame,
    OfficialVisit,
    BonusPoints,
}

enum class RecruitingSpendFailure {
    InsufficientPoints,
    InvalidBonusAmount,
    ActionWindowClosed,
    AlreadyUsedThisSeason,
    ReservationAlreadyActive,
    ScholarshipsNotOpen,
}

data class RecruitingSpendResult(
    val action: RecruitingPointAction,
    val pointsCharged: Int,
    val pointsRemaining: Int,
    /** Reservations refunded on week advance (6.18). */
    val countsAsWeekReservation: Boolean,
    val success: Boolean,
    val failure: RecruitingSpendFailure? = null,
    val interestDeltaApplied: Int = 0,
)

/**
 * Action costs from balance JSON — non-refunded spends stay spent for the week
 * ([planning/recruiting-engine.md#action-costs-design-lock]).
 */
object RecruitingActionSpend {
    fun spend(
        action: RecruitingPointAction,
        pointsAvailable: Int,
        actions: BalanceRecruitingActions,
        bonusAmount: Int? = null,
    ): RecruitingSpendResult {
        val cost = when (action) {
            RecruitingPointAction.ScoutTheirGame,
            RecruitingPointAction.OfficialVisit,
            -> return failure(action, pointsAvailable, RecruitingSpendFailure.ActionWindowClosed)
            RecruitingPointAction.BonusPoints -> {
                val amount = bonusAmount
                    ?: return failure(action, pointsAvailable, RecruitingSpendFailure.InvalidBonusAmount)
                if (amount <= 0 || amount > pointsAvailable) {
                    return failure(action, pointsAvailable, RecruitingSpendFailure.InvalidBonusAmount)
                }
                require(actions.bonusPoints.spendUnspentWeeklyBalance)
                amount
            }
            else -> entryFor(action, actions).cost
        }
        if (cost > pointsAvailable) {
            return failure(action, pointsAvailable, RecruitingSpendFailure.InsufficientPoints)
        }
        val entry = if (action == RecruitingPointAction.BonusPoints) {
            BalanceRecruitingActionEntry(cost = cost, refunded = actions.bonusPoints.refunded)
        } else {
            entryFor(action, actions)
        }
        return RecruitingSpendResult(
            action = action,
            pointsCharged = cost,
            pointsRemaining = pointsAvailable - cost,
            countsAsWeekReservation = entry.refunded,
            success = true,
        )
    }

    fun catalogCost(action: RecruitingPointAction, actions: BalanceRecruitingActions): Int =
        when (action) {
            RecruitingPointAction.BonusPoints -> 0
            RecruitingPointAction.ScoutTheirGame -> actions.scoutTheirGame.cost
            RecruitingPointAction.OfficialVisit -> actions.officialVisit.costByDistanceBand.regional
            else -> entryFor(action, actions).cost
        }

    private fun entryFor(action: RecruitingPointAction, actions: BalanceRecruitingActions): BalanceRecruitingActionEntry =
        when (action) {
            RecruitingPointAction.ScholarshipOffer -> actions.scholarshipOffer
            RecruitingPointAction.Dm -> actions.dm
            RecruitingPointAction.PhoneCall -> actions.phoneCall
            RecruitingPointAction.SendTheHouse -> actions.sendTheHouse
            RecruitingPointAction.ScoutTheirGame,
            RecruitingPointAction.OfficialVisit,
            RecruitingPointAction.BonusPoints,
            -> error("use spendRefundableAction or explicit cost for $action")
        }

    private fun failure(
        action: RecruitingPointAction,
        pointsAvailable: Int,
        reason: RecruitingSpendFailure,
    ): RecruitingSpendResult =
        RecruitingSpendResult(
            action = action,
            pointsCharged = 0,
            pointsRemaining = pointsAvailable,
            countsAsWeekReservation = false,
            success = false,
            failure = reason,
        )
}
