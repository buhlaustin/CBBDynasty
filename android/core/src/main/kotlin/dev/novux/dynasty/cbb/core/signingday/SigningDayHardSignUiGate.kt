package dev.novux.dynasty.cbb.core.signingday

/**
 * Signing Day required-task gating for Positions tab actions
 * ([planning/offseason.md], [MEGA-RECR-103]).
 */
object SigningDayHardSignUiGate {
    fun canConfirmPositions(progress: SigningDayProgress?): Boolean {
        val p = progress ?: return false
        return p.rankingsViewed && !p.positionsConfirmed && !p.hardSignComplete
    }

    fun canHardSign(progress: SigningDayProgress?, allSigneesHavePromises: Boolean): Boolean {
        val p = progress ?: return false
        return p.requiredTasksComplete() && !p.hardSignComplete && allSigneesHavePromises
    }

    fun blockingReasons(progress: SigningDayProgress?, allSigneesHavePromises: Boolean): List<SigningDayHardSignBlockReason> {
        val p = progress ?: return listOf(SigningDayHardSignBlockReason.ProgressLoading)
        val reasons = mutableListOf<SigningDayHardSignBlockReason>()
        if (p.hardSignComplete) reasons.add(SigningDayHardSignBlockReason.AlreadySigned)
        if (!p.rankingsViewed) reasons.add(SigningDayHardSignBlockReason.RankingsNotViewed)
        if (!p.positionsConfirmed) reasons.add(SigningDayHardSignBlockReason.PositionsNotConfirmed)
        if (!allSigneesHavePromises && reasons.none { it == SigningDayHardSignBlockReason.AlreadySigned }) {
            reasons.add(SigningDayHardSignBlockReason.PromisesMissing)
        }
        return reasons
    }
}

enum class SigningDayHardSignBlockReason {
    ProgressLoading,
    RankingsNotViewed,
    PositionsNotConfirmed,
    PromisesMissing,
    AlreadySigned,
}
