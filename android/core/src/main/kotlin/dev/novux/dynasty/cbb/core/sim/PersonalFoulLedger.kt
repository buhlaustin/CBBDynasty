package dev.novux.dynasty.cbb.core.sim

import kotlin.random.Random

/** NCAA personal fouls — disqualification at 5 ([planning/simulation-engine.md], MEGA-SIM-101). */
object PersonalFoulRules {
    const val DISQUALIFICATION_FOUL_COUNT: Int = 5
}

data class PersonalFoulRecord(
    val playerId: String,
    val foulCount: Int,
    val disqualified: Boolean,
)

class PersonalFoulLedger(
    initial: Map<String, Int> = emptyMap(),
) {
    private val fouls: MutableMap<String, Int> = initial.toMutableMap()

    fun foulCount(playerId: String): Int = fouls[playerId] ?: 0

    fun isDisqualified(playerId: String): Boolean =
        foulCount(playerId) >= PersonalFoulRules.DISQUALIFICATION_FOUL_COUNT

    fun record(playerId: String): PersonalFoulRecord {
        val next = foulCount(playerId) + 1
        fouls[playerId] = next
        return PersonalFoulRecord(
            playerId = playerId,
            foulCount = next,
            disqualified = next >= PersonalFoulRules.DISQUALIFICATION_FOUL_COUNT,
        )
    }

    fun snapshot(): Map<String, Int> = fouls.toMap()
}

object PersonalFoulAssignment {
    /** Picks a rotation player weighted by target minutes (proxy for floor time). */
    fun pickPlayer(roster: GameSimRosterInput, ledger: PersonalFoulLedger, random: Random): String? {
        val pool = roster.participants.filter { !ledger.isDisqualified(it.playerId) }
        if (pool.isEmpty()) return null
        val total = pool.sumOf { it.targetMinutes.coerceAtLeast(1) }
        var roll = random.nextInt(total)
        for (p in pool) {
            roll -= p.targetMinutes.coerceAtLeast(1)
            if (roll < 0) return p.playerId
        }
        return pool.last().playerId
    }
}
