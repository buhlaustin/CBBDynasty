package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.lineup.RotationMinutesAllocator
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertEquals
import org.junit.Test

class HalftimeRosterAdjustTest {
  @Test
  fun rideStartersIncreasesStarterMinutesWithinCap() {
      val roster = GameLineupPrep.build(
          teamId = "t",
          players = sampleRoster(),
      )
      val adjusted = HalftimeRosterAdjust.applyRotationEmphasis(
          roster,
          HalftimeRotationEmphasis.RideStarters,
      )
      assertEquals(RotationMinutesAllocator.TEAM_MINUTES_REGULATION, adjusted.participants.sumOf { it.targetMinutes })
      val starterIds = GameLineupPrep.resolveStarters(sampleRoster(), null).values.toSet()
      val starterAvg = adjusted.participants
          .filter { it.playerId in starterIds }
          .map { it.targetMinutes }
          .average()
      val benchAvg = adjusted.participants
          .filter { it.playerId !in starterIds }
          .map { it.targetMinutes }
          .average()
      assertEquals(true, starterAvg > benchAvg)
  }

  private fun sampleRoster(): List<RosterPlayer> = listOf(
      player("pg", PlayerPosition.PointGuard),
      player("sg", PlayerPosition.ShootingGuard),
      player("sf", PlayerPosition.SmallForward),
      player("pf", PlayerPosition.PowerForward),
      player("c", PlayerPosition.Center),
      player("b1", PlayerPosition.ShootingGuard, depth = 6),
      player("b2", PlayerPosition.PowerForward, depth = 7),
  )

  private fun player(id: String, pos: PlayerPosition, depth: Int = 1, starter: Boolean = depth <= 5): RosterPlayer =
      RosterPlayer(
          playerId = id,
          dynastyId = "d",
          position = pos,
          classYear = PlayerClassYear.Sophomore,
          isStarter = starter,
          depthOrder = depth,
          inTransferPortal = false,
          displayName = id,
          off = 70,
          def = 68,
      )
}
