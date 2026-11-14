package dev.novux.dynasty.cbb.core.sim

/** Team fouls in the current half or OT segment ([MEGA-SIM-095], MEGA-SIM-105). */
class TeamFoulHalfLedger(
    initialHome: Int = 0,
    initialAway: Int = 0,
) {
    var homeFoulsInHalf: Int = initialHome
        private set
    var awayFoulsInHalf: Int = initialAway
        private set

    fun recordDefensiveFoul(defenseIsHome: Boolean): Int =
        if (defenseIsHome) {
            homeFoulsInHalf += 1
            homeFoulsInHalf
        } else {
            awayFoulsInHalf += 1
            awayFoulsInHalf
        }

    fun reset() {
        homeFoulsInHalf = 0
        awayFoulsInHalf = 0
    }

    fun snapshot(): Pair<Int, Int> = homeFoulsInHalf to awayFoulsInHalf
}
