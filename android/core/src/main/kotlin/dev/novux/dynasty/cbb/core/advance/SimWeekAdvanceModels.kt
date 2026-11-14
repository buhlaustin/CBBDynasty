package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState

enum class SimWeekConfirmationLineKind {
    UnplayedGameFastSim,
    UserRecruitingAutoMaintain,
    LeagueRecruitingBatch,
    PortalRecruitingWeek,
    PortalRecruitingWindowClosed,
    OffseasonPortalOptionalNudges,
    PortalUserSchoolAutoSpend,
    InjuryWeeksTick,
    InjuryWeeksTickByeWeek,
    LeagueWeekBatch,
    SimpleAdvance,
}

data class SimWeekConfirmationLine(
    val kind: SimWeekConfirmationLineKind,
    val formatArgs: List<Int> = emptyList(),
    val stringFormatArgs: List<String> = emptyList(),
)

data class SimWeekConfirmation(
    val needsDialog: Boolean,
    val nextWeekLabel: String,
    val lines: List<SimWeekConfirmationLine>,
)

data class SimWeekAdvanceInput(
    val nextWeekLabel: String,
    val unplayedUserGameCount: Int,
    val recruitingPointsAvailable: Int,
    val userManualRecruitingSpent: Int,
    val recruitingHubOpen: Boolean,
    val estimatedAutoRecruitingSpend: Int,
    val portalRecruitingWeekOpen: Boolean,
    val unresolvedOptionalTaskCount: Int = 0,
    val injuredPlayersTicking: Int = 0,
    val userOnByeWeek: Boolean = false,
    val recruitingAutoMaintainEnabled: Boolean = true,
    /** Required when [unplayedUserGameCount] > 0 and no provisional mark is staged. */
    val userLeagueFastSim: UserLeagueFastSimBundle? = null,
    /** Opponent display name for unplayed-game confirmation copy ([MEGA-FRAME-023]). */
    val unplayedUserGameOpponentDisplayName: String? = null,
    /** Regular-season advance runs 365-team batch sim + rankings ([planning/time-system.md]). */
    val simulatesLeagueWeekBatch: Boolean = false,
    val offseasonMilestoneAdvance: Boolean = false,
    val portalRecruitingWindowClosed: Boolean = false,
    val inSeasonDestinationAcknowledgment: Boolean = false,
)

data class SimWeekAdvanceResolution(
    val checkpointAfterPreAdvance: WeekCheckpointState,
    val fastSimmedGames: Int,
)
