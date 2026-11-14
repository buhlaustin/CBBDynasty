package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalFoulLedgerTest {
    @Test
    fun fifthFoulDisqualifies() {
        val ledger = PersonalFoulLedger()
        repeat(4) {
            val record = ledger.record("p1")
            assertFalse(record.disqualified)
        }
        val fifth = ledger.record("p1")
        assertEquals(5, fifth.foulCount)
        assertTrue(fifth.disqualified)
        assertTrue(ledger.isDisqualified("p1"))
    }

    @Test
    fun pickPlayer_skipsDisqualified() {
        val ledger = PersonalFoulLedger()
        repeat(5) { ledger.record("p1") }
        val roster = GameSimRosterInput(
            teamId = "t",
            participants = listOf(
                GameSimParticipant("p1", PlayerPosition.Center, PlayerPosition.Center, 70, 70, 28),
                GameSimParticipant("p2", PlayerPosition.PointGuard, PlayerPosition.PointGuard, 72, 71, 32),
            ),
        )
        val pick = PersonalFoulAssignment.pickPlayer(roster, ledger, kotlin.random.Random(1))
        assertEquals("p2", pick)
    }
}
