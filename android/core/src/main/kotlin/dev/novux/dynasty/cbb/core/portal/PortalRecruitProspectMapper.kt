package dev.novux.dynasty.cbb.core.portal

import dev.novux.dynasty.cbb.core.player.scout.ScoutDepth
import dev.novux.dynasty.cbb.core.recruiting.RecruitPoolKind
import dev.novux.dynasty.cbb.core.recruiting.RecruitScoutLetterEngine
import dev.novux.dynasty.cbb.core.recruiting.RecruitPriorityRoll
import dev.novux.dynasty.cbb.core.recruiting.RecruitProspect
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import kotlin.math.abs

/** Maps portal entrants into recruit prospects for shared hub economy/UI ([planning/transfer-portal.md]). */
object PortalRecruitProspectMapper {
    fun toRecruitProspect(
        entrant: PortalEntrant,
        priorSchoolLabel: String,
    ): RecruitProspect {
        val stars = entrant.stars ?: starsFromHidden(entrant.hiddenRating)
        val position = PlayerPosition.entries[abs(entrant.entrantId.hashCode()) % PlayerPosition.entries.size]
        val homeState = stateFromProgram(entrant.priorProgramId)
        val draft = RecruitProspect(
            prospectId = entrant.entrantId,
            displayName = "Portal — $priorSchoolLabel",
            stars = stars,
            position = position,
            homeState = homeState,
            topPriorities = RecruitPriorityRoll.roll(entrant.entrantId),
            nationalHsRank = entrant.nationalTop40Rank,
            nilAskUsd = nilAskForStars(stars),
            scoutLetterOverall = "C",
            poolKind = RecruitPoolKind.Portal,
            hiddenRating = entrant.hiddenRating,
        )
        val letters = RecruitScoutLetterEngine.fromProspect(draft, ScoutDepth.Light)
        return draft.copy(scoutLetterOverall = letters.overall)
    }

    private fun starsFromHidden(hidden: Int): Int = when {
        hidden >= 92 -> 5
        hidden >= 82 -> 4
        hidden >= 70 -> 3
        else -> 2
    }

    private fun nilAskForStars(stars: Int): Int = when (stars) {
        5 -> 180_000
        4 -> 95_000
        3 -> 45_000
        else -> 18_000
    }

    private fun stateFromProgram(programId: String): String {
        val digits = programId.filter { it.isDigit() }.takeLast(2)
        val bucket = digits.toIntOrNull() ?: abs(programId.hashCode())
        return STATE_CODES[bucket % STATE_CODES.size]
    }

    private val STATE_CODES = listOf(
        "OH", "TX", "FL", "CA", "NC", "GA", "PA", "KY", "IN", "MI",
    )
}
