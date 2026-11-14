package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.lineup.RotationMinutesAllocator
import dev.novux.dynasty.cbb.core.qa.TeamPlayerMinutesPath

/**
 * Five players × elapsed game clock → 200 regulation player-minutes per team (MEGA-SIM-109).
 */
object TeamPlayerMinutesCoherence {
    val QA_ID: String = TeamPlayerMinutesPath.SIM_ID

    const val FLOOR_PLAYERS: Int = FoulTroubleRotationPolicy.FLOOR_PLAYERS

    /** Wall-clock seconds accrue to [FLOOR_PLAYERS] on-floor players per team each trip. */
    fun expectedTeamFloorSeconds(overtimePeriods: Int): Int {
        val elapsed =
            GameSimClock.REGULATION_SECONDS + overtimePeriods * GameSimClock.OVERTIME_SECONDS
        return FLOOR_PLAYERS * elapsed
    }

    fun expectedTeamFloorSeconds(result: GameSimResult): Int =
        expectedTeamFloorSeconds(result.overtimePeriods)

    fun expectedRegulationPlayerMinutes(): Int = RotationMinutesAllocator.TEAM_MINUTES_REGULATION

    fun teamFloorSeconds(players: List<PlayerBoxScore>): Int =
        players.sumOf { it.minutesSeconds }

    fun reconcileFloorMinutes(ledger: PlayerBoxScoreLedger, expectedTeamFloorSeconds: Int) {
        val lines = ledger.snapshot()
        if (lines.isEmpty()) return
        val actual = lines.sumOf { it.minutesSeconds }
        val gap = expectedTeamFloorSeconds - actual
        if (gap <= 0) return
        val anchor = lines.maxByOrNull { it.minutesSeconds }?.playerId ?: lines.first().playerId
        ledger.addMinutesSeconds(anchor, gap)
    }

    fun assertTeamMinutes(result: GameSimResult) {
        if (!result.isComplete) return
        assertTeamSide(result.homePlayerBox, expectedTeamFloorSeconds(result), "home")
        assertTeamSide(result.awayPlayerBox, expectedTeamFloorSeconds(result), "away")
    }

    private fun assertTeamSide(players: List<PlayerBoxScore>, expectedSeconds: Int, label: String) {
        if (players.isEmpty()) return
        val actual = teamFloorSeconds(players)
        val tolerance = FLOOR_PLAYERS * GameSimClock.MAX_TRIP_SECONDS
        require(actual in (expectedSeconds - tolerance)..(expectedSeconds + tolerance)) {
            "$label team floor seconds $actual not within $tolerance of $expectedSeconds " +
                "(≈${expectedRegulationPlayerMinutes()} player-minutes regulation + OT)"
        }
        require(teamFloorSeconds(players) == expectedSeconds) {
            "$label team floor seconds must equal $expectedSeconds after reconcile"
        }
    }
}
