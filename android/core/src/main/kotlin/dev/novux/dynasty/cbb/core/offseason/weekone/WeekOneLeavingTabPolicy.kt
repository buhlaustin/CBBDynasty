package dev.novux.dynasty.cbb.core.offseason.weekone

import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** Week 1 Roster / Transfer tab semantics ([planning/transfer-portal.md#week-1--roster-vs-transfer-tabs]). */
object WeekOneLeavingTabPolicy {
    const val MEGA_FRAME_ID: String = "MEGA-FRAME-059"
    const val ROSTER_TAB_INDEX: Int = 0
    const val TRANSFER_TAB_INDEX: Int = 1

    fun rosterPlayers(players: List<RosterPlayer>): List<RosterPlayer> =
        PortalWeekOnePartition.onRosterTab(players)

    fun transferPlayers(players: List<RosterPlayer>): List<RosterPlayer> =
        PortalWeekOnePartition.onTransferTab(players)
}
