package dev.novux.dynasty.cbb.core.news

import dev.novux.dynasty.cbb.core.league.LeagueStandingsState

/** Weekly NET-based bracketology copy for the news feed ([MEGA-GDD-130]). */
object BracketologyNewsEngine {
    const val MEGA_ID: String = "MEGA-GDD-130"

    fun headlineForWeek(
        standings: LeagueStandingsState,
        rsWeekIndex: Int,
        teamDisplayName: (String) -> String,
        userTeamId: String? = null,
    ): NewsHeadline? {
        if (!BracketologyNewsPolicy.shouldPublish(rsWeekIndex)) return null
        val ranks = standings.netRankByTeam
        if (ranks.isEmpty()) return null

        val ordered = ranks.entries.sortedBy { it.value }
        val locks = ordered.take(BracketologyNewsPolicy.LOCK_LINE_COUNT)
            .joinToString(", ") { teamDisplayName(it.key) }
        val bubbleStart = (52 - BracketologyNewsPolicy.BUBBLE_LINE_COUNT)
            .coerceAtLeast(BracketologyNewsPolicy.LOCK_LINE_COUNT + 1)
        val bubble = ordered
            .drop(bubbleStart - 1)
            .take(BracketologyNewsPolicy.BUBBLE_LINE_COUNT)
            .joinToString(", ") { teamDisplayName(it.key) }

        val userNote = userTeamId?.let { id ->
            ranks[id]?.let { rank ->
                val label = teamDisplayName(id)
                when {
                    rank <= 16 -> "$label NET #$rank (lock line)"
                    rank in 45..62 -> "$label NET #$rank (bubble watch)"
                    rank in 17..44 -> "$label NET #$rank (at-large mix)"
                    else -> "$label NET #$rank"
                }
            }
        }

        val title = buildString {
            append("Bracketology (Week $rsWeekIndex): ")
            append("locks — ")
            append(locks)
            if (bubble.isNotBlank()) {
                append("; bubble — ")
                append(bubble)
            }
            userNote?.let {
                append(". ")
                append(it)
            }
        }

        return NewsHeadline(
            id = "bracketology-${standings.seasonIndex}-w$rsWeekIndex",
            seasonIndex = standings.seasonIndex,
            rsWeekIndex = rsWeekIndex,
            kind = NewsHeadlineKind.Bracketology,
            title = title,
            priority = 45,
            createdAtMs = System.currentTimeMillis(),
        )
    }
}
