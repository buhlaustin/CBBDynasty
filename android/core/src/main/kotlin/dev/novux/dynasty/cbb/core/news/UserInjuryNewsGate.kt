package dev.novux.dynasty.cbb.core.news

import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.injuries.InjurySeverity
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** Filters user sim injuries that earn a shallow-feed headline ([MEGA-SIM-164]). */
object UserInjuryNewsGate {
    fun newsworthy(
        events: List<GameSimInjuryEvent>,
        players: List<RosterPlayer>,
        headlineOverallMin: Int,
    ): List<GameSimInjuryEvent> {
        val ovrById = players.associate { it.playerId to it.ovr }
        return events.filter { event ->
            val ovr = ovrById[event.playerId] ?: 0
            event.severity == InjurySeverity.Major ||
                event.severity == InjurySeverity.Moderate ||
                ovr >= headlineOverallMin
        }
    }
}
