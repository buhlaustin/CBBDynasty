package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.balance.BalanceInjuryOccurrence
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.injuries.InjuryBankRow
import dev.novux.dynasty.cbb.core.injuries.InjurySeverity
import dev.novux.dynasty.cbb.core.injuries.MidGameInjurySampler
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

class MidGameInjurySamplerTest {
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

    private fun player(
        id: String,
        position: PlayerPosition,
        starter: Boolean,
        depth: Int,
        slotWire: String? = null,
    ) = RosterPlayer(
        playerId = id,
        dynastyId = "d",
        position = position,
        classYear = PlayerClassYear.Junior,
        isStarter = starter,
        depthOrder = depth,
        inTransferPortal = false,
        displayName = id,
        ovr = 75,
        off = 75,
        def = 72,
        starterSlotWire = slotWire,
    )

    @Test
    fun midGameInjuryStopsFurtherMinuteAccrual() {
        val roster = listOf(
            player("pg", PlayerPosition.PointGuard, starter = true, depth = 1, slotWire = "pg"),
            player("sg", PlayerPosition.ShootingGuard, starter = true, depth = 1, slotWire = "sg"),
            player("sf", PlayerPosition.SmallForward, starter = true, depth = 1, slotWire = "sf"),
            player("pf", PlayerPosition.PowerForward, starter = true, depth = 1, slotWire = "pf"),
            player("c", PlayerPosition.Center, starter = true, depth = 1, slotWire = "c"),
            player("b6", PlayerPosition.ShootingGuard, starter = false, depth = 2),
            player("b7", PlayerPosition.SmallForward, starter = false, depth = 3),
            player("b8", PlayerPosition.PowerForward, starter = false, depth = 4),
        )
        val simRoster = GameLineupPrep.build("h", roster)
        val rollInput = GameSimInjuryRollInput(
            phase = CalendarPhase.RegularSeason,
            bank = bank,
            occurrence = BalanceInjuryOccurrence(baseChancePerPlayerGame = 4.0),
            medicalStaffGrade = 70,
            homeRosterPlayers = roster,
            awayRosterPlayers = null,
        )
        val input = GameSimInput(
            home = GameSimTeamInput("h", 75, 72),
            away = GameSimTeamInput("a", 70, 70),
            tuning = tuning,
            seed = 1203L,
            homeRoster = simRoster,
        )
        val session = PossessionSimSession(input, injuryRoll = rollInput)
        var injuredId: String? = null
        var minutesAtInjury = 0.0
        while (session.hasNext()) {
            val before = session.toMidGame().homeFloorSecondsByPlayer.toMap()
            session.step()
            val mid = session.toMidGame()
            val injuryLine = mid.possessionLog.lastOrNull { it.startsWith("INJURY —") }
            if (injuryLine != null && injuredId == null) {
                injuredId = mid.homeMidGameInjuries.firstOrNull()?.playerId
                minutesAtInjury = (before[injuredId] ?: 0) / 60.0
            }
            if (injuredId != null) {
                val afterSec = mid.homeFloorSecondsByPlayer[injuredId] ?: 0
                assertTrue(
                    "Injured player minutes must not grow after injury",
                    afterSec <= (minutesAtInjury * 60).toInt() + 30,
                )
            }
        }
        val result = session.toResult(isComplete = true)
        val final = GameSimInjuryRoll.attach(input, result, rollInput)
        assertTrue(final.homeInjuries.isNotEmpty())
        assertTrue(final.possessionLog.any { it.startsWith("INJURY —") })
        val hurt = checkNotNull(injuredId)
        assertTrue(final.homeInjuries.any { it.playerId == hurt })
        assertTrue(final.homeInjuries.first { it.playerId == hurt }.occurredInGameMinute != null)
    }

    @Test
    fun logLineMatchesSpecFormat() {
        val line = MidGameInjurySampler.formatLogLine("Jordan Lee", "Ankle sprain")
        assertTrue(line.startsWith("INJURY —"))
        assertTrue(line.contains("Jordan Lee"))
        assertTrue(line.contains("Ankle sprain"))
    }
}
