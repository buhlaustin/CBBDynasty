package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.PersonalFoulDisqualificationPath

/**
 * Five personal fouls — player leaves for good; logged on session (MEGA-SIM-108).
 */
object PersonalFoulDisqualificationPolicy {
    val QA_ID: String = PersonalFoulDisqualificationPath.SIM_ID

    fun disqualifiedIdsFromSnapshot(personalFouls: Map<String, Int>): Set<String> =
        personalFouls.filterValues { it >= PersonalFoulRules.DISQUALIFICATION_FOUL_COUNT }.keys

    fun registerIfDisqualified(
        record: PersonalFoulRecord,
        disqualifiedOut: MutableSet<String>,
    ) {
        if (record.disqualified) {
            disqualifiedOut += record.playerId
        }
    }

    fun dqBannerLine(teamLabel: String, record: PersonalFoulRecord): String =
        "— DQ $teamLabel ${record.playerId} (${record.foulCount} PF) — cannot re-enter"

    fun filterEligibleOnFloor(
        playerIds: List<String>,
        foulLedger: PersonalFoulLedger,
        disqualifiedOut: Set<String>,
    ): List<String> =
        playerIds.filter { id ->
            id !in disqualifiedOut && !foulLedger.isDisqualified(id)
        }

    fun filterRosterFallback(
        roster: GameSimRosterInput?,
        foulLedger: PersonalFoulLedger,
        disqualifiedOut: Set<String>,
    ): List<String> =
        roster?.participants
            ?.map { it.playerId }
            ?.filter { id -> id !in disqualifiedOut && !foulLedger.isDisqualified(id) }
            ?.take(FoulTroubleRotationPolicy.FLOOR_PLAYERS)
            .orEmpty()
}
