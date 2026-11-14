package dev.novux.dynasty.cbb.core.sim

/** Stable jersey # for possession log when roster has no explicit uniform (MEGA-SIM-097). */
object SimJerseyNumber {
    fun fromPlayerId(playerId: String): Int =
        1 + (playerId.hashCode() and 0x7fffffff) % 54
}
