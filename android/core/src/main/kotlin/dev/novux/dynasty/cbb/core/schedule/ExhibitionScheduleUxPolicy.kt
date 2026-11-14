package dev.novux.dynasty.cbb.core.schedule

/** Preseason task 7 — exhibition builder ([planning/preseason.md#7--schedule-exhibition-games], [MEGA-FRAME-075]). */
object ExhibitionScheduleUxPolicy {
    const val MEGA_FRAME_ID: String = "MEGA-FRAME-075"

    val exhibitionWeekIndices: IntRange = 1..PreseasonScheduleConstants.MAX_EXHIBITION_GAMES

    fun validateDraft(count: Int, drafts: List<ExhibitionGameDraft>): Boolean =
        draftIssue(count, drafts) == null

    /** Why confirm is disabled — surfaced on checklist + builder ([MEGA-FRAME-134]). */
    fun draftIssue(count: Int, drafts: List<ExhibitionGameDraft>): ExhibitionDraftIssue? {
        if (count == 0) return null
        if (drafts.size != count) return ExhibitionDraftIssue.DraftCountMismatch
        if (drafts.any { it.opponentTeamId.isBlank() }) return ExhibitionDraftIssue.MissingOpponent
        val weeks = drafts.map { it.exhibitionWeekIndex }
        if (!weeks.all { it in exhibitionWeekIndices }) return ExhibitionDraftIssue.InvalidWeek
        if (weeks.distinct().size != weeks.size) return ExhibitionDraftIssue.DuplicateWeek
        return null
    }

    fun draftsToStubs(drafts: List<ExhibitionGameDraft>): List<ScheduledGameStub> =
        drafts
            .sortedBy { it.exhibitionWeekIndex }
            .map {
                ScheduledGameStub(
                    kind = ScheduleGameKind.Exhibition,
                    opponentTeamId = it.opponentTeamId,
                    opponentDisplayName = it.opponentDisplayName,
                    homeGame = it.homeGame,
                    rsWeekIndex = it.exhibitionWeekIndex,
                )
            }
}

enum class ExhibitionDraftIssue {
    DraftCountMismatch,
    MissingOpponent,
    InvalidWeek,
    DuplicateWeek,
}

data class ExhibitionGameDraft(
    val opponentTeamId: String = "",
    val opponentDisplayName: String = "",
    val homeGame: Boolean = true,
    val exhibitionWeekIndex: Int = 1,
)
