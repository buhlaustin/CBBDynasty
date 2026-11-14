package dev.novux.dynasty.cbb.core.sim

/** Per-participant shooting tendency for possession resolution ([MEGA-SIM-169]). */
data class GameSimShootingProfile(
    val threePointTendency: Int = 50,
)
