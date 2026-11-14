package dev.novux.dynasty.cbb.core.balance

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class BalanceRecruitingActions(
    val scholarshipOffer: BalanceRecruitingActionEntry = BalanceRecruitingActionEntry(cost = 150, refunded = true),
    val dm: BalanceRecruitingActionEntry = BalanceRecruitingActionEntry(cost = 50, refunded = false),
    val phoneCall: BalanceRecruitingActionEntry = BalanceRecruitingActionEntry(cost = 100, refunded = false),
    val sendTheHouse: BalanceRecruitingActionEntry = BalanceRecruitingActionEntry(cost = 500, refunded = false),
    val scoutTheirGame: BalanceRecruitingScoutEntry = BalanceRecruitingScoutEntry(),
    val officialVisit: BalanceRecruitingVisitEntry = BalanceRecruitingVisitEntry(),
    val bonusPoints: BalanceRecruitingBonusEntry = BalanceRecruitingBonusEntry(),
)

@Serializable
data class BalanceRecruitingActionEntry(
    val cost: Int,
    val refunded: Boolean = false,
    val refundPolicy: String? = null,
    val interestDelta: Int = 0,
)

@Serializable
data class BalanceRecruitingScoutEntry(
    val cost: Int = 50,
    val refunded: Boolean = true,
    val refundPolicy: String? = null,
    val oncePerRecruitPerSeason: Boolean = true,
    val allowedPhases: List<String> = emptyList(),
)

@Serializable
data class BalanceRecruitingVisitEntry(
    val costByDistanceBand: BalanceVisitDistanceCosts = BalanceVisitDistanceCosts(),
    val refunded: Boolean = true,
    val refundPolicy: String? = null,
    val oncePerRecruitPerSeason: Boolean = true,
    val allowedThrough: BalanceVisitAllowedThrough? = null,
)

@Serializable
data class BalanceVisitDistanceCosts(
    val local: Int = 50,
    val regional: Int = 100,
    val remote: Int = 150,
    val interestDeltaLocal: Int = 12,
    val interestDeltaRegional: Int = 15,
    val interestDeltaRemote: Int = 18,
)

@Serializable
data class BalanceVisitAllowedThrough(
    val phase: String = "offseason",
    val weekIndexMax: Int = 5,
    val includesRegularSeason: Boolean = true,
)

@Serializable
data class BalanceRecruitingBonusEntry(
    val refunded: Boolean = false,
    val spendUnspentWeeklyBalance: Boolean = true,
    /** Points spent per +1 interest when using bonus balance (e.g. 25 → 1). */
    val interestDeltaPerPoint: Int = 25,
)

object BalanceRecruitingActionsParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(recruiting: JsonElement): BalanceRecruitingActions {
        val obj = recruiting as? JsonObject ?: return BalanceRecruitingActions()
        val actions = obj["actions"] ?: return BalanceRecruitingActions()
        return json.decodeFromJsonElement(BalanceRecruitingActions.serializer(), actions)
    }
}
