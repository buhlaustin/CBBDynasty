package dev.novux.dynasty.cbb.core.injuries

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

data class InjuryWeekTickResult(
    val players: List<RosterPlayer>,
    val returnedPlayerIds: List<String>,
)

data class InjuryApplyContext(
    val rsWeekIndex: Int,
    val rsGameIndex: Int,
    val rsGamesPlayed: Int,
    val stackPolicyReplace: Boolean = true,
)

object InjuryRosterOps {
    fun applyGameInjuries(
        players: List<RosterPlayer>,
        events: List<GameSimInjuryEvent>,
        context: InjuryApplyContext,
    ): List<RosterPlayer> {
        if (events.isEmpty()) return players
        var roster = players
        events.forEach { event ->
            roster = roster.map { player ->
                if (player.playerId != event.playerId) return@map player
                if (player.injuryActive && !context.stackPolicyReplace) return@map player
                player.copy(
                    injuryActive = true,
                    injuryId = event.injuryId,
                    injuryLabel = event.label,
                    injuryWeeksRemaining = event.weeksRemaining,
                    injurySeverityWire = event.severity.wire,
                    injuryOccurredAtRsWeek = context.rsWeekIndex,
                    injuryOccurredAtRsGameIndex = context.rsGameIndex,
                    rsGamesPlayedAtInjury = context.rsGamesPlayed,
                )
            }
            roster = InjuryLineupAdjust.applyInjuryLineupAdjust(
                roster,
                injuredPlayerIds = setOf(event.playerId),
            )
        }
        return roster
    }

    fun tickWeekAdvance(
        phaseCommitted: CalendarPhase,
        players: List<RosterPlayer>,
    ): InjuryWeekTickResult {
        if (!InjuryCalendarPolicy.ticksOnWeekAdvance(phaseCommitted)) {
            return InjuryWeekTickResult(players, emptyList())
        }
        val returned = mutableListOf<String>()
        var next = players.map { player ->
            if (!player.injuryActive || player.medicalRedshirtSeason) return@map player
            val weeks = player.injuryWeeksRemaining - 1
            if (weeks <= 0) {
                returned += player.playerId
                player.copy(
                    injuryActive = false,
                    injuryWeeksRemaining = 0,
                    injuryId = null,
                    injuryLabel = null,
                    injurySeverityWire = null,
                )
            } else {
                player.copy(injuryWeeksRemaining = weeks)
            }
        }
        val returnOrder = returned.sortedBy { id ->
            val wire = next.firstOrNull { it.playerId == id }?.injuryWasStarterSlotWire
            val slot = wire?.let(PlayerPosition::fromWire)
            if (slot != null) PlayerPosition.STARTER_ORDER.indexOf(slot) else PlayerPosition.STARTER_ORDER.size
        }
        returnOrder.forEach { id ->
            next = InjuryLineupAdjust.applyInjuryReturnLineupAdjust(next, id)
        }
        return InjuryWeekTickResult(next, returned)
    }
}
