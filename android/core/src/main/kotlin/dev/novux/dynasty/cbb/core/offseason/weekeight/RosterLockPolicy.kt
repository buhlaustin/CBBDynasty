package dev.novux.dynasty.cbb.core.offseason.weekeight

import dev.novux.dynasty.cbb.core.roster.RosterPlayer

object RosterLockPolicy {
    fun scholarshipCount(players: List<RosterPlayer>): Int =
        players.count { it.classYear != dev.novux.dynasty.cbb.core.roster.PlayerClassYear.WalkOn }

    fun walkOnCount(players: List<RosterPlayer>): Int =
        players.count { it.classYear == dev.novux.dynasty.cbb.core.roster.PlayerClassYear.WalkOn }

    fun canAdvanceOffseasonWeek8(players: List<RosterPlayer>): Boolean =
        scholarshipCount(players) <= OffseasonRosterLockConstants.SCHOLARSHIP_ROSTER_CAP

    fun canOfferWalkOn(players: List<RosterPlayer>, walkOnsSignedThisSeason: Int): Boolean =
        walkOnsSignedThisSeason < OffseasonRosterLockConstants.MAX_WALK_ONS_PER_SEASON &&
            scholarshipCount(players) < OffseasonRosterLockConstants.SCHOLARSHIP_ROSTER_CAP
}
