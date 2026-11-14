package dev.novux.dynasty.cbb.core.player.card

import dev.novux.dynasty.cbb.core.player.DynastyPlayer

/** Portal + draft declare status for player card chips ([MEGA-GDD-024]). */
object PlayerRosterStatusCardPolicy {
    const val MEGA_ID: String = "MEGA-GDD-024"

    enum class ChipKind {
        TransferPortal,
        DeclaredForDraft,
        ProDayComplete,
        DraftStock,
    }

    fun visibleChips(player: DynastyPlayer): List<ChipKind> = buildList {
        if (player.role.inTransferPortal) add(ChipKind.TransferPortal)
        if (player.role.declaredForDraft) {
            add(ChipKind.DeclaredForDraft)
            if (player.role.proDayCompleted) add(ChipKind.ProDayComplete)
            if (player.role.draftStockPoints > 0) add(ChipKind.DraftStock)
        }
    }
}