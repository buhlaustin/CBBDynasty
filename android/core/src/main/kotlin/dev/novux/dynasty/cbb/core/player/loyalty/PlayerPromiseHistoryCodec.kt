package dev.novux.dynasty.cbb.core.player.loyalty

object PlayerPromiseHistoryCodec {
    private const val SEP_ENTRY = ";"
    private const val SEP_PAIR = ":"

    fun encode(entries: List<PlayerPromiseHistoryEntry>): String =
        entries
            .sortedBy { it.seasonIndex }
            .joinToString(SEP_ENTRY) { "${it.seasonIndex}$SEP_PAIR${it.status.wire}" }

    fun decode(wire: String): List<PlayerPromiseHistoryEntry> {
        if (wire.isBlank()) return emptyList()
        return wire.split(SEP_ENTRY)
            .mapNotNull { token ->
                val parts = token.split(SEP_PAIR, limit = 2)
                if (parts.size != 2) return@mapNotNull null
                val season = parts[0].toIntOrNull() ?: return@mapNotNull null
                val status = PromiseStatus.fromWire(parts[1]) ?: return@mapNotNull null
                PlayerPromiseHistoryEntry(seasonIndex = season, status = status)
            }
    }

    fun upsert(wire: String, entry: PlayerPromiseHistoryEntry): String {
        val merged = decode(wire)
            .filterNot { it.seasonIndex == entry.seasonIndex }
            .plus(entry)
        return encode(merged)
    }
}
