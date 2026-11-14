package dev.novux.dynasty.cbb.core.player.loyalty

import dev.novux.dynasty.cbb.core.recruiting.RecruitOfferPromise

/** Locks the single offer-time promise at hard commit ([MEGA-COACH-175]). */
data class RecruitLockedCommitPromise(
    val prospectId: String,
    val promiseType: CoachPromiseType,
    val stayTopN: Int?,
    val lockedAtSeasonIndex: Int,
) {
    fun toCoachPromise(): CoachPromise = CoachPromise(
        type = promiseType,
        stayTopN = stayTopN,
        lockedAtSeasonIndex = lockedAtSeasonIndex,
        status = PromiseStatus.Pending,
    )
}

object RecruitCommitPromiseLockEngine {
    const val MEGA_ID: String = "MEGA-COACH-175"

    fun lockFromOffer(
        offer: RecruitOfferPromise,
        lockedAtSeasonIndex: Int,
    ): RecruitLockedCommitPromise = RecruitLockedCommitPromise(
        prospectId = offer.prospectId,
        promiseType = offer.promiseType,
        stayTopN = offer.stayTopN,
        lockedAtSeasonIndex = lockedAtSeasonIndex,
    )

    fun allSigneesHaveOffer(
        prospectIds: Collection<String>,
        offers: Map<String, RecruitOfferPromise>,
    ): Boolean = prospectIds.all { offers.containsKey(it) }
}

/** No mid-career promise swap — spec lock ([MEGA-COACH-175]). */
object PlayerPromiseAmendPolicy {
    const val ALLOW_AMEND_UI: Boolean = false
}
