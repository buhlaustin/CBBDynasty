package dev.novux.dynasty.cbb.core.recruiting

/**
 * Scholarship offers with a binding promise add a small pre-sign interest bump ([MEGA-RECR-090]).
 */
object RecruitScholarshipPromiseDeltaPolicy {
    const val MEGA_ID: String = "MEGA-RECR-090"

    const val DEFAULT_PROMISE_DELTA: Int = 6

    fun promiseDelta(offer: RecruitOfferPromise?): Int =
        if (offer != null) DEFAULT_PROMISE_DELTA else 0
}