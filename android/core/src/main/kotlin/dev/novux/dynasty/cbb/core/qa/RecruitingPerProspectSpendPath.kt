package dev.novux.dynasty.cbb.core.qa

/**
 * Hub point actions attribute spend per `prospectId`, not only the global cycle ledger ([MEGA-QA-068]).
 */
object RecruitingPerProspectSpendPath {
    const val QA_ID: String = "MEGA-QA-068"

    const val RECR_CROSS_REF: String = "MEGA-RECR-022"

    const val CORE_LEDGER: String =
        "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/recruiting/RecruitingProspectPointSpendLedger.kt"

    const val ECONOMY_REPOSITORY: String =
        "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/recruiting/RecruitingEconomyRepository.kt"

    const val INTEREST_SPEND_REPOSITORY: String =
        "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/recruiting/RecruitingInterestSpendRepository.kt"

    const val RECORD_METHOD: String = "recordProspectSpend"

    const val PERSIST_METHOD: String = "addPointSpend"
}
