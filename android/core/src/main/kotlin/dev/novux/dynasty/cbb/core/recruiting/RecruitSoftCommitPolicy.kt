package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.signingday.SigningDaySoftCommitInputs

/**
 * Soft commit when user leads the influence board and closeness crosses the commit band
 * ([MEGA-RECR-029], [planning/recruiting-engine.md#influence-board-design-lock]).
 */
object RecruitSoftCommitPolicy {
    const val RECR_ID: String = "MEGA-RECR-029"

    const val REQUIRED_USER_RANK: Int = 1

    const val MIN_CLOSENESS: Int = 70

    fun qualifies(board: ProspectInfluenceBoard, hasScholarshipOffer: Boolean): Boolean =
        hasScholarshipOffer &&
            board.userRank == REQUIRED_USER_RANK &&
            board.closeness >= MIN_CLOSENESS

    fun influenceBoard(
        prospect: RecruitProspect,
        inputs: SigningDaySoftCommitInputs,
        hasScholarshipOffer: Boolean,
    ): ProspectInfluenceBoard {
        val base = InfluenceBoardEngine.build(
            prospect = prospect,
            userSchool = inputs.userSchool,
            userProgramId = inputs.userProgramId,
            recommended = inputs.recommended,
            userHasScholarshipOffer = hasScholarshipOffer,
        )
        val spendBoost = (inputs.spendDeltaByProspect[prospect.prospectId] ?: 0) / 5
        return base.copy(closeness = (base.closeness + spendBoost).coerceIn(0, 100))
    }
}
