package dev.novux.dynasty.cbb.core.signingday

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterConstants
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import kotlin.math.roundToInt

/** Hard sign attaches incoming freshmen to the dynasty roster scholarship pool ([MEGA-RECR-109]). */
object SigningDayRosterEnrollPolicy {
    const val MEGA_ID: String = "MEGA-RECR-109"

    fun rosterCap(): Int = RosterConstants.SCHOLARSHIP_CAP

    fun enrollSignee(
        dynastyId: String,
        signee: IncomingSignee,
        depthOrder: Int,
        existingScholarshipCount: Int,
    ): RosterPlayer? {
        if (existingScholarshipCount >= rosterCap()) return null
        val ovr = headlineOvr(signee)
        return RosterPlayer(
            playerId = signee.prospectId,
            dynastyId = dynastyId,
            position = signee.position,
            classYear = PlayerClassYear.Freshman,
            isStarter = false,
            depthOrder = depthOrder,
            inTransferPortal = false,
            displayName = signee.displayName,
            hometownState = signee.homeState,
            nilUsdPerYear = signee.nilSignedUsd.toLong().coerceAtLeast(signee.nilAskUsd.toLong()),
            ovr = ovr,
            off = ovr,
            def = (ovr - 4).coerceAtLeast(45),
        )
    }

    private fun headlineOvr(signee: IncomingSignee): Int {
        val base = when (signee.stars) {
            5 -> 78
            4 -> 70
            3 -> 62
            else -> 55
        }
        val letterBonus = signee.scoutLetterOverall.firstOrNull()?.let { letter ->
            when (letter.uppercaseChar()) {
                'A' -> 4
                'B' -> 2
                else -> 0
            }
        } ?: 0
        return (base + letterBonus).coerceIn(50, 92)
    }
}