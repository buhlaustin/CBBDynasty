package dev.novux.dynasty.cbb.core.carousel

import dev.novux.dynasty.cbb.core.coach.employment.CoachFiringEngine

/** League-wide AI firings, extensions, and hire backfill ([MEGA-GDD-119]). */
object CarouselLeagueBackgroundSimEngine {
    const val MEGA_ID: String = "MEGA-GDD-119"

    data class CarouselBackgroundSimSummary(
        val aiFired: Int = 0,
        val aiExtended: Int = 0,
        val aiHired: Int = 0,
        val userFired: Boolean = false,
        val userPoachImmunity: Boolean = false,
    )

    data class CarouselBackgroundSimResult(
        val summary: CarouselBackgroundSimSummary,
        val userRequiresHeadCoachOffer: Boolean,
    )

    fun run(context: CarouselSimContext): CarouselBackgroundSimResult {
        val random = context.deterministicRandom()
        var aiFired = 0
        var aiExtended = 0
        var userFired = false
        var userPoachImmunity = false
        var userRequiresOffer = context.userUnemployed

        context.leagueCoaches.forEach { row ->
            if (row.isUserProgram) {
                val securityScore = context.userSecurity?.jobSecurity?.score
                    ?: row.jobSecurityScore
                userPoachImmunity = CarouselAiBehaviorTable.shouldPoachImmunity(row)
                if (!context.userUnemployed &&
                    CoachFiringEngine.shouldEnterUnemployed(securityScore, context.seasonIndex, context.dynastyId)
                ) {
                    userFired = true
                    userRequiresOffer = true
                }
                return@forEach
            }

            val risk = CarouselAiBehaviorTable.totalFiringRisk(row)
            val roll = random.nextInt(100)
            val fired = roll < risk && risk >= CarouselAiBehaviorTable.FIRING_RISK_FIRING_THRESHOLD
            if (fired) {
                aiFired++
                return@forEach
            }

            val affinity = CarouselAiBehaviorTable.extensionAffinity(row)
            val extends = affinity >= CarouselAiBehaviorTable.EXTENSION_MIN_AFFINITY &&
                row.contractYearsLeft <= 3
            if (extends) {
                aiExtended++
            }
        }

        val aiHired = aiFired
        val summary = CarouselBackgroundSimSummary(
            aiFired = aiFired,
            aiExtended = aiExtended,
            aiHired = aiHired,
            userFired = userFired,
            userPoachImmunity = userPoachImmunity,
        )
        return CarouselBackgroundSimResult(summary, userRequiresOffer)
    }
}
