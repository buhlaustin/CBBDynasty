package dev.novux.dynasty.cbb.core.qa

/**
 * kotlinx.serialization survives R8 on release paths ([MEGA-QA-152]).
 */
object ReleaseSerializationR8Path {
    const val QA_ID: String = "MEGA-QA-152"

    const val PROGUARD_RULES: String = "android/app/proguard-rules.pro"

    /** Types that must round-trip JSON on new-game + load after minify. */
    val CRITICAL_SERIALIZABLE_TYPES: List<String> = listOf(
        "dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameBoxArchive",
        "dev.novux.dynasty.cbb.core.sim.tuning.SimTuning",
        "dev.novux.dynasty.cbb.core.balance.BalanceRuleset",
        "dev.novux.dynasty.cbb.core.league.LeagueStandingsState",
    )

    const val SMOKE_TEST_CLASS: String =
        "dev.novux.dynasty.cbb.core.qa.ReleaseSerializationR8SmokeTest"
}
