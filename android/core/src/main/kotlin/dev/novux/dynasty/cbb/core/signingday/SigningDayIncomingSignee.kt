package dev.novux.dynasty.cbb.core.signingday

import dev.novux.dynasty.cbb.core.recruiting.RecruitProspect
import dev.novux.dynasty.cbb.core.recruiting.RecruitSoftCommitPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingRefundableAction
import dev.novux.dynasty.cbb.core.recruiting.RecruitingWeekReservation
import dev.novux.dynasty.cbb.core.roster.PlayerPosition

data class IncomingSignee(
    val prospectId: String,
    val displayName: String,
    val stars: Int,
    val position: PlayerPosition,
    val homeState: String,
    val nilAskUsd: Int,
    val nilSignedUsd: Int = 0,
    val nationalHsRank: Int?,
    val scoutLetterOverall: String,
)

object SigningDaySoftCommitResolver {
    fun incomingSoftCommits(
        pool: List<RecruitProspect>,
        offerReservations: List<RecruitingWeekReservation>,
        inputs: SigningDaySoftCommitInputs,
    ): List<IncomingSignee> {
        val offerIds = offerReservations
            .filter { it.action == RecruitingRefundableAction.ScholarshipOffer }
            .map { it.prospectId }
            .toSet()
        return pool
            .filter { it.prospectId in offerIds }
            .filter { prospect ->
                val board = RecruitSoftCommitPolicy.influenceBoard(
                    prospect = prospect,
                    inputs = inputs,
                    hasScholarshipOffer = true,
                )
                RecruitSoftCommitPolicy.qualifies(board, hasScholarshipOffer = true)
            }
            .map { it.toSignee() }
    }
}

fun RecruitProspect.toSignee(positionOverride: PlayerPosition? = null): IncomingSignee =
    IncomingSignee(
        prospectId = prospectId,
        displayName = displayName,
        stars = stars,
        position = positionOverride ?: position,
        homeState = homeState,
        nilAskUsd = nilAskUsd,
        nationalHsRank = nationalHsRank,
        scoutLetterOverall = scoutLetterOverall,
    )
