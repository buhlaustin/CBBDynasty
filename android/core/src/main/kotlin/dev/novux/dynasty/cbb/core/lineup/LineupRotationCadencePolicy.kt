package dev.novux.dynasty.cbb.core.lineup

import dev.novux.dynasty.cbb.core.roster.PlayerPosition

/** PG–C slots and rotation minutes on Strategy ([MEGA-GDD-066]). */
object LineupRotationCadencePolicy {
    const val MEGA_ID: String = "MEGA-GDD-066"

    val starterSlotCount: Int = PlayerPosition.entries.size
}