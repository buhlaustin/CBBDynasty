package dev.novux.dynasty.cbb.core.signingday

import dev.novux.dynasty.cbb.core.player.loyalty.RecruitCommitPromiseLockEngine
import dev.novux.dynasty.cbb.core.recruiting.RecruitOfferPromise

data class SigningDayProgress(
    val cycleSeasonIndex: Int,
    val rankingsViewed: Boolean = false,
    val positionsConfirmed: Boolean = false,
    val hardSignComplete: Boolean = false,
) {
    fun requiredTasksComplete(): Boolean = rankingsViewed && positionsConfirmed

    fun canAdvanceOffseasonWeek(): Boolean = hardSignComplete
}

data class SigningDayHardSignResult(
    val signed: List<IncomingSignee>,
    val progress: SigningDayProgress,
)

object SigningDayHardSignEngine {
    fun finalizeClass(
        progress: SigningDayProgress,
        signees: List<IncomingSignee>,
        offerPromisesByProspect: Map<String, RecruitOfferPromise>,
    ): SigningDayHardSignResult {
        require(progress.requiredTasksComplete()) {
            "Signing Day required tasks incomplete"
        }
        require(
            RecruitCommitPromiseLockEngine.allSigneesHaveOffer(
                signees.map { it.prospectId },
                offerPromisesByProspect,
            ),
        ) {
            "Each signee needs a coach promise before hard sign"
        }
        return SigningDayHardSignResult(
            signed = signees,
            progress = progress.copy(hardSignComplete = true),
        )
    }
}
