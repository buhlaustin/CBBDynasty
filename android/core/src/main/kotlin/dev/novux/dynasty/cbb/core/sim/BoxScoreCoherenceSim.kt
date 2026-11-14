package dev.novux.dynasty.cbb.core.sim

/**
 * Release path: retry sim with bumped seeds when coherence fails
 * ([planning/simulation-engine.md#statistical-coherence], MEGA-SIM-135).
 */
object BoxScoreCoherenceSim {
    const val MAX_RETRIES: Int = 8

    fun simulateWithRetry(
        input: GameSimInput,
        options: SimulateOptions = SimulateOptions(),
        injuryRoll: GameSimInjuryRollInput? = null,
    ): GameSimResult {
        var lastError: IllegalArgumentException? = null
        repeat(MAX_RETRIES) { attempt ->
            val attemptInput = input.copy(seed = input.seed + attempt)
            try {
                val result = PossessionSimEngine.simulateUnchecked(attemptInput, options, injuryRoll)
                BoxScoreCoherence.assertValid(result)
                return result
            } catch (e: IllegalArgumentException) {
                lastError = e
            }
        }
        throw IllegalStateException(
            "Box score coherence failed after $MAX_RETRIES attempts (seed=${input.seed})",
            lastError,
        )
    }
}
