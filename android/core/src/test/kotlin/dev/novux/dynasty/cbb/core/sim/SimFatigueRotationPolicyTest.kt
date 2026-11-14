package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.balance.BalanceFatigue
import dev.novux.dynasty.cbb.core.fatigue.FatigueEngine
import dev.novux.dynasty.cbb.core.qa.SimFatigueRotationPath
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimFatigueRotationPolicyTest {
  private val tuning = BalanceFatigue.DEFAULT

    @Test
    fun megaSim104_qaAnchor() {
        assertEquals(SimFatigueRotationPath.SIM_ID, SimFatigueRotationPolicy.QA_ID)
    }

    @Test
    fun tiredStarterPulledForFreshReserve() {
        val roster = GameLineupPrep.build(
            "t",
            listOf(
                RosterPlayer(
                    playerId = "starter",
                    dynastyId = "d",
                    position = PlayerPosition.PointGuard,
                    classYear = PlayerClassYear.Senior,
                    isStarter = true,
                    depthOrder = 1,
                    inTransferPortal = false,
                    stamina = 30,
                    off = 80,
                    def = 78,
                    ovr = 79,
                ),
                RosterPlayer(
                    playerId = "reserve",
                    dynastyId = "d",
                    position = PlayerPosition.ShootingGuard,
                    classYear = PlayerClassYear.Junior,
                    isStarter = true,
                    depthOrder = 2,
                    inTransferPortal = false,
                    stamina = FatigueEngine.FRESH,
                    off = 75,
                    def = 74,
                    ovr = 74,
                ),
                RosterPlayer(
                    playerId = "b2",
                    dynastyId = "d",
                    position = PlayerPosition.SmallForward,
                    classYear = PlayerClassYear.Sophomore,
                    isStarter = true,
                    depthOrder = 3,
                    inTransferPortal = false,
                    stamina = FatigueEngine.FRESH,
                    off = 72,
                    def = 72,
                    ovr = 72,
                ),
                RosterPlayer(
                    playerId = "b3",
                    dynastyId = "d",
                    position = PlayerPosition.PowerForward,
                    classYear = PlayerClassYear.Sophomore,
                    isStarter = true,
                    depthOrder = 4,
                    inTransferPortal = false,
                    stamina = FatigueEngine.FRESH,
                    off = 71,
                    def = 71,
                    ovr = 71,
                ),
                RosterPlayer(
                    playerId = "b4",
                    dynastyId = "d",
                    position = PlayerPosition.Center,
                    classYear = PlayerClassYear.Freshman,
                    isStarter = true,
                    depthOrder = 5,
                    inTransferPortal = false,
                    stamina = FatigueEngine.FRESH,
                    off = 70,
                    def = 70,
                    ovr = 70,
                ),
            ),
        )
        val stamina = SimStaminaLedger(roster, mapOf("starter" to 20))
        val foul = PersonalFoulLedger()
        val minutes = SimFloorMinutesLedger()
        val foulFloor = FoulTroubleRotationPolicy.floorParticipants(roster, foul, GameSimClock.HALF_SECONDS)
        val floor = RotationBandFloorPolicy.applyMinuteBands(
            roster,
            foulFloor,
            foul,
            minutes,
            GameSimClock.HALF_SECONDS,
            stamina,
            tuning,
        )
        assertFalse(floor.any { it.playerId == "starter" })
        assertTrue(floor.any { it.playerId == "reserve" })
    }

    @Test
    fun staminaLedger_drainsOnFloorTime() {
        val roster = GameSimRosterInput(
            "t",
            listOf(
                GameSimParticipant(
                    "p1",
                    PlayerPosition.PointGuard,
                    PlayerPosition.PointGuard,
                    80,
                    78,
                    30,
                    startingStamina = 100,
                ),
            ),
        )
        val ledger = SimStaminaLedger(roster)
        ledger.drainFloorTime(listOf("p1"), seconds = 600, tuning = tuning)
        assertTrue(ledger.stamina("p1") < 100)
    }
}
