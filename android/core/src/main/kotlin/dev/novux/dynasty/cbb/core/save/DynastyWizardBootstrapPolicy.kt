package dev.novux.dynasty.cbb.core.save

/**
 * Wizard sign must wire recruiting economy + NIL for the chosen school ([planning/game-start.md],
 * MEGA-RECR-076, MEGA-RECR-100).
 */
object DynastyWizardBootstrapPolicy {
    enum class Step {
        IncomingClassPool,
        Top60NewsSpotlight,
        RecruitingLedgerPrestige,
        NilAnnualCyclePool,
    }

    fun stepsAfterSign(seasonIndex: Int): List<Step> =
        if (seasonIndex == 1) {
            listOf(
                Step.IncomingClassPool,
                Step.Top60NewsSpotlight,
                Step.RecruitingLedgerPrestige,
                Step.NilAnnualCyclePool,
            )
        } else {
            emptyList()
        }

    fun requiresNilCycleAtSign(seasonIndex: Int): Boolean =
        Step.NilAnnualCyclePool in stepsAfterSign(seasonIndex)
}
