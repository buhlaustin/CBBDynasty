package dev.novux.dynasty.cbb.core.player.draft

import dev.novux.dynasty.cbb.core.player.DynastyPlayer

/** Stamps national Top 60 rank onto the card-bound player ([MEGA-COACH-163]). */
fun DynastyPlayer.withNationalTop60DraftRank(rankMap: Map<String, Int>): DynastyPlayer =
    copy(nationalTop60DraftRank = rankMap[playerId])
