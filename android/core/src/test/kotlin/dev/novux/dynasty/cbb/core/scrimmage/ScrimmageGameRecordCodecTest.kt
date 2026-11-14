package dev.novux.dynasty.cbb.core.scrimmage

import dev.novux.dynasty.cbb.core.calendar.checkpoint.ArchivedTeamBoxScore
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameBoxArchive
import org.junit.Assert.assertEquals
import org.junit.Test

class ScrimmageGameRecordCodecTest {
    @Test
    fun roundTrip() {
        val record = ScrimmageGameRecord(
            dynastyId = "d1",
            opponentLabel = "Rival",
            homeScore = 70,
            awayScore = 65,
            archive = ProvisionalGameBoxArchive(
                homeBox = ArchivedTeamBoxScore("h", 70, 25, 50, 8, 20, 10, 12, 9, 60, 14),
                awayBox = ArchivedTeamBoxScore("a", 65, 24, 48, 7, 18, 9, 11, 11, 58, 16),
            ),
        )
        val decoded = ScrimmageGameRecordCodec.decode(ScrimmageGameRecordCodec.encode(record))
        assertEquals(record.dynastyId, decoded.dynastyId)
        assertEquals(record.homeScore, decoded.homeScore)
        assertEquals(record.archive.homeBox.points, decoded.archive.homeBox.points)
    }
}
