package dev.novux.dynasty.cbb.core.advance

/**
 * Sim-week copy when injuries tick on a calendar bye ([planning/injuries.md], [MEGA-FRAME-080]).
 */
object InjuryByeAdvanceConfirmPolicy {
    const val MEGA_FRAME_ID: String = "MEGA-FRAME-080"

    fun injuryTickLineKind(input: SimWeekAdvanceInput): SimWeekConfirmationLineKind? {
        if (input.injuredPlayersTicking <= 0) return null
        return if (input.userOnByeWeek && input.unplayedUserGameCount == 0) {
            SimWeekConfirmationLineKind.InjuryWeeksTickByeWeek
        } else {
            SimWeekConfirmationLineKind.InjuryWeeksTick
        }
    }

    /** Bye + injured-only advances still need the destination week label in the dialog body. */
    fun appendSimpleAdvanceAfterInjuryBye(input: SimWeekAdvanceInput): Boolean =
        input.userOnByeWeek &&
            input.injuredPlayersTicking > 0 &&
            input.unplayedUserGameCount == 0 &&
            input.unresolvedOptionalTaskCount == 0 &&
            !input.portalRecruitingWeekOpen &&
            !(input.recruitingHubOpen && input.userManualRecruitingSpent == 0 && input.recruitingPointsAvailable > 0)
}
