package dev.novux.dynasty.cbb.core.calendar.checkpoint

import dev.novux.dynasty.cbb.core.sim.TeamBoxScore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProvisionalGameBoxCodecTest {
    @Test
    fun roundTripsArchive() {
        val archive = ProvisionalGameBoxArchive(
            homeBox = ArchivedTeamBoxScore.from(TeamBoxScore("h", points = 72)),
            awayBox = ArchivedTeamBoxScore.from(TeamBoxScore("a", points = 68)),
        )
        val encoded = ProvisionalGameBoxCodec.encode(archive)
        val decoded = ProvisionalGameBoxCodec.decode(encoded)
        assertNotNull(decoded)
        assertEquals(72, decoded!!.homeBox.points)
        assertEquals(68, decoded.awayBox.points)
    }
}
