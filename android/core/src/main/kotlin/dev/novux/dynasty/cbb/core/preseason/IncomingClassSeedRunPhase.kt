package dev.novux.dynasty.cbb.core.preseason

/** Preseason task 1 — stepped run-seed UX ([planning/preseason.md#1--incoming-class-seed], [MEGA-FRAME-071]). */
enum class IncomingClassSeedRunPhase {
    RollingHighSchoolPool,
    RollingJucoPool,
    RankingNationalRecruits,
    PublishingTop60Headline,
    SavingClassPool,
    Done,
}

object IncomingClassSeedRunProgress {
    const val MEGA_FRAME_ID: String = "MEGA-FRAME-071"

    fun fraction(phase: IncomingClassSeedRunPhase): Float = when (phase) {
        IncomingClassSeedRunPhase.RollingHighSchoolPool -> 0.2f
        IncomingClassSeedRunPhase.RollingJucoPool -> 0.45f
        IncomingClassSeedRunPhase.RankingNationalRecruits -> 0.65f
        IncomingClassSeedRunPhase.PublishingTop60Headline -> 0.82f
        IncomingClassSeedRunPhase.SavingClassPool -> 0.94f
        IncomingClassSeedRunPhase.Done -> 1f
    }
}
