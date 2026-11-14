package dev.novux.dynasty.cbb.core.player.loyalty

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerPromiseHistoryCodecTest {
    @Test
    fun encodeDecode_roundTrip() {
        val entries = listOf(
            PlayerPromiseHistoryEntry(1, PromiseStatus.Kept),
            PlayerPromiseHistoryEntry(2, PromiseStatus.Broken),
        )
        val wire = PlayerPromiseHistoryCodec.encode(entries)
        assertEquals(entries, PlayerPromiseHistoryCodec.decode(wire))
    }

    @Test
    fun upsert_replacesSeason() {
        val wire = PlayerPromiseHistoryCodec.encode(
            listOf(PlayerPromiseHistoryEntry(1, PromiseStatus.AtRisk)),
        )
        val next = PlayerPromiseHistoryCodec.upsert(
            wire,
            PlayerPromiseHistoryEntry(1, PromiseStatus.Kept),
        )
        assertEquals(PromiseStatus.Kept, PlayerPromiseHistoryCodec.decode(next).single().status)
    }
}
