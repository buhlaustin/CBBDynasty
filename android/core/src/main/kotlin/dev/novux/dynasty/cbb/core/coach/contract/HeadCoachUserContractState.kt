package dev.novux.dynasty.cbb.core.coach.contract

import kotlinx.serialization.Serializable

@Serializable
data class HeadCoachUserContractState(
    val dynastyId: String,
    val contractLengthYears: Int = 4,
    val contractYearsLeft: Int = 4,
    val annualSalaryUsd: Long? = null,
    val declinedExtensionSeasonIndex: Int? = null,
) {
    companion object {
        fun defaults(dynastyId: String) = HeadCoachUserContractState(dynastyId = dynastyId)
    }
}
