package dev.novux.dynasty.cbb.core.signingday

import dev.novux.dynasty.cbb.core.recruiting.IncomingClassSeeder

/**
 * Signing Day rankings tab is **recruit** national Top 60 — not program NET/polls ([MEGA-QA-112]).
 */
object SigningDayUiPolicy {
    const val DEFAULT_TAB_INDEX: Int = 0
    const val RANKINGS_TAB_IS_RECRUIT_NATIONAL: Boolean = true
    const val RANKINGS_LIST_CAP: Int = IncomingClassSeeder.TOP_NATIONAL_RANKS
}
