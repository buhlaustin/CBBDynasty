package dev.novux.dynasty.cbb.core.sim.tuning

import kotlinx.serialization.json.JsonObject

object SimTuningValidator {
    fun validate(
        tuning: SimTuning,
        expectedTuningId: String? = null,
        expectedSchemaVersion: Int? = null,
    ): SimTuningValidationError? {
        expectedTuningId?.let { expected ->
            if (tuning.simTuningId != expected) {
                return SimTuningValidationError.TuningIdMismatch(expected, tuning.simTuningId)
            }
        }
        expectedSchemaVersion?.let { expected ->
            if (tuning.schemaVersion != expected) {
                return SimTuningValidationError.SchemaVersionMismatch(expected, tuning.schemaVersion)
            }
        }
        if (tuning.simTuningId.isBlank()) {
            return SimTuningValidationError.MalformedJson("simTuningId blank")
        }
        if (tuning.schemaVersion < 1) {
            return SimTuningValidationError.MalformedJson("schemaVersion must be >= 1")
        }

        validateIntMinMax("possessionsPerGameTarget", tuning.possessionsPerGameTarget)?.let { return it }

        if (tuning.shotClock.full <= 0) {
            return SimTuningValidationError.InvalidRange("shotClock.full", "must be positive")
        }
        if (tuning.shotClock.offensiveRebound <= 0) {
            return SimTuningValidationError.InvalidRange("shotClock.offensiveRebound", "must be positive")
        }

        validateTimeBand("timeUsedSeconds.quickTurnover", tuning.timeUsedSeconds.quickTurnover)?.let { return it }
        validateTimeBand("timeUsedSeconds.quickShot", tuning.timeUsedSeconds.quickShot)?.let { return it }
        validateTimeBand("timeUsedSeconds.foulStoppage", tuning.timeUsedSeconds.foulStoppage)?.let { return it }

        if (tuning.eventWeights !is JsonObject) {
            return SimTuningValidationError.MissingSection("eventWeights")
        }
        val resolvedWeights = SimEventWeightsResolver.resolve(tuning.eventWeights)
        SimEventWeightsResolver.validate(resolvedWeights)?.let { msg ->
            return SimTuningValidationError.InvalidRange("eventWeights", msg)
        }

        if (tuning.homeNeutralModifiers.home <= 0.0 ||
            tuning.homeNeutralModifiers.neutral <= 0.0 ||
            tuning.homeNeutralModifiers.road <= 0.0
        ) {
            return SimTuningValidationError.InvalidRange(
                "homeNeutralModifiers",
                "home, neutral, and road must be positive",
            )
        }

        val foulRules = tuning.teamFoulRules
        if (foulRules.bonusThreshold <= 0 || foulRules.doubleBonusThreshold <= 0) {
            return SimTuningValidationError.InvalidRange(
                "teamFoulRules",
                "bonus and doubleBonus thresholds must be positive",
            )
        }
        if (foulRules.bonusThreshold > foulRules.doubleBonusThreshold) {
            return SimTuningValidationError.InvalidRange(
                "teamFoulRules",
                "bonusThreshold must be <= doubleBonusThreshold",
            )
        }

        val comfort = tuning.positionComfort
        if (comfort.eligibleInterchangePenalty !in 0..15 || comfort.ineligibleSlotPenalty !in 0..15) {
            return SimTuningValidationError.InvalidRange(
                "positionComfort",
                "penalties must be within 0..15",
            )
        }

        return null
    }

    private fun validateIntMinMax(field: String, band: IntMinMax): SimTuningValidationError? {
        if (band.min <= 0 || band.max <= 0) {
            return SimTuningValidationError.InvalidRange(field, "min and max must be positive")
        }
        if (band.min > band.max) {
            return SimTuningValidationError.InvalidRange(field, "min must be <= max")
        }
        return null
    }

    private fun validateTimeBand(field: String, values: List<Int>): SimTuningValidationError? {
        if (values.size != 2) {
            return SimTuningValidationError.InvalidRange(field, "expected [min, max] pair (size 2)")
        }
        val (min, max) = values
        if (min <= 0 || max <= 0) {
            return SimTuningValidationError.InvalidRange(field, "values must be positive")
        }
        if (min > max) {
            return SimTuningValidationError.InvalidRange(field, "min must be <= max")
        }
        return null
    }
}
