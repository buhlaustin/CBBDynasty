package dev.novux.dynasty.cbb.core.advance

/** Portal slice of offseason sim-week confirmation ([planning/transfer-portal.md#ux--dashboard-optional-tasks-portal-only]). */
object OffseasonPortalSimWeekPolicy {
    const val MEGA_FRAME_ID: String = "MEGA-FRAME-068"

    fun showPortalOptionalNudgeLine(input: SimWeekAdvanceInput): Boolean =
        input.portalRecruitingWeekOpen && input.unresolvedOptionalTaskCount > 0

    fun showPortalUserAutoSpendLine(input: SimWeekAdvanceInput): Boolean =
        input.portalRecruitingWeekOpen &&
            input.unresolvedOptionalTaskCount > 0 &&
            input.recruitingAutoMaintainEnabled &&
            input.recruitingHubOpen &&
            input.userManualRecruitingSpent == 0 &&
            input.recruitingPointsAvailable > 0

    fun usePortalSpendLineInsteadOfGenericAutoMaintain(input: SimWeekAdvanceInput): Boolean =
        showPortalUserAutoSpendLine(input)
}
