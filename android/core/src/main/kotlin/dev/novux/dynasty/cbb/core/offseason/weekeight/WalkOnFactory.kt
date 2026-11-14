package dev.novux.dynasty.cbb.core.offseason.weekeight

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** Walk-on tryouts — up to two per season at week 8 roster lock ([MEGA-GDD-022], [planning/offseason.md]). */
object WalkOnFactory {
    const val MEGA_GDD_ID = "MEGA-GDD-022"
    fun create(
        dynastyId: String,
        seasonIndex: Int,
        walkOnIndex: Int,
        position: PlayerPosition,
        depthOrder: Int,
    ): RosterPlayer =
        RosterPlayer(
            playerId = "walkon-$dynastyId-$seasonIndex-$walkOnIndex",
            dynastyId = dynastyId,
            position = position,
            classYear = PlayerClassYear.WalkOn,
            isStarter = false,
            depthOrder = depthOrder,
            inTransferPortal = false,
        )
}
