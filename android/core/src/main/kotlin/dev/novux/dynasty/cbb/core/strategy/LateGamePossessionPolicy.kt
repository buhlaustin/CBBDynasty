package dev.novux.dynasty.cbb.core.strategy

/**
 * Late-game foul strategy possession decisions — not only scalar defense tweaks (MEGA-SIM-112).
 */
object LateGamePossessionPolicy {
    const val LATE_POSSESSION_WINDOW_SECONDS: Int = 4 * 60
    @Deprecated("Use SimFoulEarlyWeights via FoulEarlyLateGamePolicy")
    const val INTENTIONAL_FOUL_TRIP_RATE: Double = 0.38
    @Deprecated("Use SimProtectLeadWeights.avoidFoulStoppageRate via ProtectLeadLateGamePolicy")
    const val PROTECT_LEAD_AVOID_FOUL_STOPPAGE_RATE: Double = 0.65

    enum class PossessionDecision {
        None,
        IntentionalFoulTrip,
        ProtectLeadPace,
    }

    fun defenseDecision(
        strategy: LateGameFoulStrategy,
        gameClockSeconds: Int,
        defenseScore: Int,
        offenseScore: Int,
    ): PossessionDecision {
        if (gameClockSeconds > LATE_POSSESSION_WINDOW_SECONDS) return PossessionDecision.None
        return when (strategy) {
            LateGameFoulStrategy.FoulEarly ->
                if (defenseScore < offenseScore) {
                    PossessionDecision.IntentionalFoulTrip
                } else {
                    PossessionDecision.None
                }
            LateGameFoulStrategy.ProtectLead ->
                if (defenseScore > offenseScore) {
                    PossessionDecision.ProtectLeadPace
                } else {
                    PossessionDecision.None
                }
            LateGameFoulStrategy.Standard -> PossessionDecision.None
        }
    }

    fun logTag(decision: PossessionDecision): String? = when (decision) {
        PossessionDecision.IntentionalFoulTrip -> "late foul-early; intentional foul branch"
        PossessionDecision.ProtectLeadPace -> "late protect"
        PossessionDecision.None -> null
    }
}
