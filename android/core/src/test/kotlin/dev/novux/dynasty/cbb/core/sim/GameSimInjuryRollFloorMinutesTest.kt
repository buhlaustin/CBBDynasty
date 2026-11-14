package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.balance.BalanceInjuryOccurrence
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.injuries.InjuryBankRow
import dev.novux.dynasty.cbb.core.injuries.InjurySeverity
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSimInjuryRollFloorMinutesTest {
    private val tuning = SimTuning(
        simTuningId = "test",
        schemaVersion = 1,
        possessionsPerGameTarget = IntMinMax(60, 70),
        shotClock = SimShotClock(30, 20),
        timeUsedSeconds = SimTimeUsedSeconds(listOf(3, 6), listOf(8, 14), listOf(12, 18)),
        eventWeights = JsonObject(emptyMap()),
        homeNeutralModifiers = SimVenueModifiers(1.0, 1.0, 1.0),
    )

    private val bank = listOf(
        InjuryBankRow(
            id = "ankle",
            label = "Ankle",
            bodyPart = "ankle",
            severity = InjurySeverity.Minor,
            durationWeeksMin = 1,
            durationWeeksMax = 1,
            weight = 1.0,
        ),
    )

    @Test
    fun postGameRollPrefersRecordedFloorMinutesOverPrepEstimate() {
        val rosterPlayers = fullRoster()
        val simRoster = GameLineupPrep.build("h", rosterPlayers)
        val input = GameSimInput(
            home = GameSimTeamInput("h", 75, 72),
            away = GameSimTeamInput("a", 70, 70),
            tuning = tuning,
            seed = 42L,
            homeRoster = simRoster,
        )
        val baseResult = GameSimResult(
            homeScore = 70,
            awayScore = 65,
            homeBox = TeamBoxScore(teamId = "h", possessions = 65),
            awayBox = TeamBoxScore(teamId = "a", possessions = 65),
            totalPossessions = 130,
            possessionLog = emptyList(),
            isComplete = true,
            homeFloorSecondsByPlayer = mapOf("pg1" to 28 * 60),
        )
        val withHigh = GameSimInjuryRoll.attach(
            input,
            baseResult,
            GameSimInjuryRollInput(
                phase = CalendarPhase.RegularSeason,
                bank = bank,
                occurrence = BalanceInjuryOccurrence(baseChancePerPlayerGame = 0.99),
                medicalStaffGrade = 70,
                homeRosterPlayers = rosterPlayers,
                awayRosterPlayers = null,
            ),
        )
        val withLow = GameSimInjuryRoll.attach(
            input,
            baseResult.copy(homeFloorSecondsByPlayer = mapOf("pg1" to 3 * 60)),
            GameSimInjuryRollInput(
                phase = CalendarPhase.RegularSeason,
                bank = bank,
                occurrence = BalanceInjuryOccurrence(baseChancePerPlayerGame = 0.99),
                medicalStaffGrade = 70,
                homeRosterPlayers = rosterPlayers,
                awayRosterPlayers = null,
            ),
        )
        assertTrue(withHigh.homeInjuries.size >= withLow.homeInjuries.size)
    }

    private fun fullRoster(): List<RosterPlayer> = listOf(
        roster("pg1", PlayerPosition.PointGuard),
        roster("sg1", PlayerPosition.ShootingGuard),
        roster("sf1", PlayerPosition.SmallForward),
        roster("pf1", PlayerPosition.PowerForward),
        roster("c1", PlayerPosition.Center),
    )

    private fun roster(id: String, pos: PlayerPosition) = RosterPlayer(
        playerId = id,
        dynastyId = "d",
        position = pos,
        classYear = PlayerClassYear.Junior,
        isStarter = true,
        depthOrder = 1,
        inTransferPortal = false,
        starterSlotWire = pos.wire,
    )
}
