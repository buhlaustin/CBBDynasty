package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.balance.BalanceFatigue
import dev.novux.dynasty.cbb.core.fatigue.FatigueEngine

/** Live stamina during possession sim ([MEGA-SIM-104]). */
class SimStaminaLedger(
    roster: GameSimRosterInput?,
    initial: Map<String, Int> = emptyMap(),
) {
    private val stamina: MutableMap<String, Int> = mutableMapOf()

    init {
        roster?.participants?.forEach { p ->
            stamina[p.playerId] = initial[p.playerId] ?: p.startingStamina
        }
        initial.forEach { (id, value) ->
            if (id !in stamina) stamina[id] = value
        }
    }

    fun stamina(playerId: String): Int =
        stamina[playerId] ?: FatigueEngine.FRESH

    fun drainFloorTime(playerIds: List<String>, seconds: Int, tuning: BalanceFatigue) {
        if (seconds <= 0) return
        playerIds.forEach { id ->
            stamina[id] = FatigueEngine.drainFromFloorSeconds(stamina(id), seconds, tuning)
        }
    }

    fun snapshot(): Map<String, Int> = stamina.toMap()
}
