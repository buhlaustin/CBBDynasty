package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.balance.BalanceInjuryOccurrence
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.injuries.InjuryBankRow
import dev.novux.dynasty.cbb.core.injuries.InjurySeverity
import dev.novux.dynasty.cbb.core.injuries.InjuryRosterOps
import dev.novux.dynasty.cbb.core.injuries.InjuryApplyContext
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSimInjuryRollTest {
    private val tuning = SimTuning(
        simTuningId = "test",
        schemaVersion = 1,
        possessionsPerGameTarget = IntMinMax(60, 70),
        shotClock = SimShotClock(full = 30, offensiveRebound = 20),
        timeUsedSeconds = SimTimeUsedSeconds(
            quickTurnover = listOf(3, 6),
            quickShot = listOf(8, 14),
            foulStoppage = listOf(12, 18),
        ),
        eventWeights = JsonObject(emptyMap()),
        homeNeutralModifiers = SimVenueModifiers(1.0, 1.0, 1.0),
    )

    private val bank = listOf(
        InjuryBankRow(
            id = "ankle_sprain",
            label = "Ankle sprain",
            bodyPart = "ankle",
            severity = InjurySeverity.Minor,
            durationWeeksMin = 1,
            durationWeeksMax = 2,
            weight = 1.0,
        ),
    )

    private fun starter(id: String, position: PlayerPosition, slotWire: String) = RosterPlayer(
        playerId = id,
        dynastyId = "d",
        position = position,
        classYear = PlayerClassYear.Junior,
        isStarter = true,
        depthOrder = 1,
        inTransferPortal = false,
        ovr = 75,
        off = 75,
        def = 72,
        starterSlotWire = slotWire,
    )

    @Test
    fun highRateRollProducesInjuryAndLineupAdjust() {
        val roster = listOf(
            starter("pg1", PlayerPosition.PointGuard, "pg"),
            starter("sg1", PlayerPosition.ShootingGuard, "sg"),
            starter("sf1", PlayerPosition.SmallForward, "sf"),
            starter("pf1", PlayerPosition.PowerForward, "pf"),
            starter("c1", PlayerPosition.Center, "c"),
        )
        val simRoster = GameLineupPrep.build("h", roster)
        val input = GameSimInput(
            home = GameSimTeamInput("h", 75, 72),
            away = GameSimTeamInput("a", 70, 70),
            tuning = tuning,
            seed = 88L,
            homeRoster = simRoster,
        )
        val rollInput = GameSimInjuryRollInput(
            phase = CalendarPhase.RegularSeason,
            bank = bank,
            occurrence = BalanceInjuryOccurrence(baseChancePerPlayerGame = 0.95),
            medicalStaffGrade = 70,
            homeRosterPlayers = roster,
            awayRosterPlayers = null,
        )
        val result = PossessionSimEngine.simulate(input, injuryRoll = rollInput)
        assertTrue(result.homeInjuries.isNotEmpty())
        val event = result.homeInjuries.first()
        assertTrue(event.weeksRemaining >= 1)
        val adjusted = InjuryRosterOps.applyGameInjuries(
            roster,
            listOf(event),
            InjuryApplyContext(rsWeekIndex = 3, rsGameIndex = 3, rsGamesPlayed = 2),
        )
        assertFalse(adjusted.first { it.playerId == event.playerId }.isStarter)
    }
}
