package dev.novux.dynasty.cbb.core.recruiting

/**
 * After hard sign closes the cycle, week advance grants no new recruiting drip ([MEGA-RECR-104]).
 */
object RecruitingClosedCycleWeekGrantPolicy {
    const val MEGA_ID: String = "MEGA-RECR-104"

    fun skipsWeeklyGrant(ledger: RecruitingCycleLedger): Boolean = ledger.cycleClosed
}