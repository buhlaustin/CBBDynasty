package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.PersonalFoulDisqualificationPath
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalFoulDisqualificationPolicyTest {
    private val roster = GameSimRosterInput(
        teamId = "t",
        participants = listOf(
            GameSimParticipant("s1", PlayerPosition.PointGuard, PlayerPosition.PointGuard, 90, 70, 32),
            GameSimParticipant("s2", PlayerPosition.ShootingGuard, PlayerPosition.ShootingGuard, 70, 88, 32),
            GameSimParticipant("b1", PlayerPosition.SmallForward, PlayerPosition.SmallForward, 75, 75, 18),
            GameSimParticipant("b2", PlayerPosition.PowerForward, PlayerPosition.PowerForward, 74, 74, 16),
            GameSimParticipant("b3", PlayerPosition.Center, PlayerPosition.Center, 72, 72, 14),
            GameSimParticipant("b4", PlayerPosition.Center, PlayerPosition.Center, 71, 71, 12),
        ),
    )

    @Test
    fun megaSim108_qaAnchor() {
        assertEquals(PersonalFoulDisqualificationPath.SIM_ID, PersonalFoulDisqualificationPolicy.QA_ID)
    }

    @Test
    fun fifthFoul_registersAndNeverReEntersFloor() {
        val ledger = PersonalFoulLedger(mapOf("s1" to 4))
        val record = ledger.record("s1")
        val disqualified = mutableSetOf<String>()
        PersonalFoulDisqualificationPolicy.registerIfDisqualified(record, disqualified)
        assertTrue(record.disqualified)
        assertEquals(setOf("s1"), disqualified.toSet())
        val floor = FoulTroubleRotationPolicy.floorParticipants(
            roster,
            ledger,
            GameSimClock.REGULATION_SECONDS,
        )
        assertFalse(floor.any { it.playerId == "s1" })
        val filtered = PersonalFoulDisqualificationPolicy.filterEligibleOnFloor(
            listOf("s1", "s2", "b1", "b2", "b3"),
            ledger,
            disqualified,
        )
        assertTrue("s1" !in filtered)
        assertEquals(4, filtered.size)
    }

    @Test
    fun dqBannerLine_documentsCannotReEnter() {
        val record = PersonalFoulRecord("pg1", 5, disqualified = true)
        val line = PersonalFoulDisqualificationPolicy.dqBannerLine("HOME", record)
        assertTrue(line.contains("DQ"))
        assertTrue(line.contains("cannot re-enter"))
        assertTrue(line.contains("pg1"))
    }

    @Test
    fun snapshot_atFiveFouls_isDisqualifiedId() {
        val ids = PersonalFoulDisqualificationPolicy.disqualifiedIdsFromSnapshot(mapOf("a" to 4, "b" to 5))
        assertEquals(setOf("b"), ids)
    }
}
