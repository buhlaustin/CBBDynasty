package dev.novux.dynasty.cbb.core.lineup

import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** User-edited rotation minutes normalized to regulation team cap. */
object RotationMinutesBalancer {
    const val PLAYER_MIN: Int = 0
    const val PLAYER_MAX: Int = 40

    fun baseline(
        players: List<RosterPlayer>,
        starters: Map<PlayerPosition, String>,
    ): Map<String, Int> =
        RotationMinutesAllocator.allocate(
            players.filter { RotationMinutesAllocator.isLineupEligible(it) },
            starters,
        )

    fun resolveForSim(
        players: List<RosterPlayer>,
        starters: Map<PlayerPosition, String>,
        userMinutes: Map<String, Int>?,
    ): Map<String, Int> {
        val eligible = players.filter { RotationMinutesAllocator.isLineupEligible(it) }
        val ids = eligible.map { it.playerId }.toSet()
        if (userMinutes.isNullOrEmpty()) {
            return RotationMinutesAllocator.allocate(eligible, starters)
        }
        val merged = baseline(players, starters).toMutableMap()
        userMinutes.filterKeys { it in ids }.forEach { (id, min) ->
            merged[id] = min.coerceIn(PLAYER_MIN, PLAYER_MAX)
        }
        return rebalance(merged, RotationMinutesAllocator.TEAM_MINUTES_REGULATION)
    }

    /** Single-player edit without auto-rebalance (shows on budget meter). */
    fun tweak(
        minutes: Map<String, Int>,
        playerId: String,
        delta: Int,
    ): Map<String, Int> {
        if (playerId !in minutes) return minutes
        val next = (minutes[playerId] ?: 0) + delta
        if (next !in PLAYER_MIN..PLAYER_MAX) return minutes
        return minutes + (playerId to next)
    }

    fun normalize(
        minutes: Map<String, Int>,
        cap: Int = RotationMinutesAllocator.TEAM_MINUTES_REGULATION,
    ): Map<String, Int> = rebalance(minutes.toMutableMap(), cap)

    @Deprecated("Use tweak + normalize", ReplaceWith("tweak(minutes, playerId, delta)"))
    fun adjust(
        minutes: Map<String, Int>,
        playerId: String,
        delta: Int,
        cap: Int = RotationMinutesAllocator.TEAM_MINUTES_REGULATION,
    ): Map<String, Int> = normalize(tweak(minutes, playerId, delta), cap)

    fun rebalance(
        minutes: MutableMap<String, Int>,
        cap: Int,
    ): Map<String, Int> {
        minutes.keys.toList().forEach { id ->
            minutes[id] = (minutes[id] ?: 0).coerceIn(PLAYER_MIN, PLAYER_MAX)
        }
        var total = minutes.values.sum()
        while (total > cap) {
            val donor = minutes.filter { it.value > PLAYER_MIN }.maxByOrNull { it.value }?.key ?: break
            minutes[donor] = minutes.getValue(donor) - 1
            total--
        }
        while (total < cap) {
            val receiver = minutes.minByOrNull { it.value }?.key ?: break
            if (minutes.getValue(receiver) >= PLAYER_MAX) break
            minutes[receiver] = minutes.getValue(receiver) + 1
            total++
        }
        return minutes.toMap()
    }
}
