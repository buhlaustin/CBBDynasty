package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId

enum class DashboardTaskKind {
    Required,
    Optional,
}

enum class DashboardDeepLink {
    RecruitingRecommended,
    RecruitingPortal,
    RecruitingHsJuco,
    SigningDayRankings,
    SigningDayPositions,
    Strategy,
    DisciplineSheet,
    @Deprecated("Use PreseasonTask* deep links", ReplaceWith("PreseasonTaskDeepLinks.deepLink(taskId)"))
    PreseasonChecklist,
    PreseasonTaskIncomingClassSeed,
    PreseasonTaskWorkouts,
    PreseasonTaskCoachUpgrades,
    PreseasonTaskRankingsReview,
    PreseasonTaskReport,
    PreseasonTaskTournamentInvites,
    PreseasonTaskScheduleExhibitions,
    PreseasonTaskScheduleRegularSeason,
    RankingsHub,
    PlayersLeavingRoster,
    PlayersLeavingTransfer,
    PlayersLeavingProDays,
    SeasonReviewRecap,
    PlayWeekGame,
    ReplayProvisionalWeekGame,
    CoachCard,
    OffseasonRosterCuts,
    OffseasonConferenceOffers,
    SummerWorkoutReport,
    FundraisingWeekNine,
    MarchMayhemHub,
    HistoryHub,
}

enum class DashboardTaskId {
    SpendRecruitingPoints,
    PortalWindow,
    PortalTop40Published,
    PortalPipelineEntrant,
    SigningCrunch,
    OpenScholarships,
    SigningDayViewRankings,
    SigningDayEditPositions,
    SigningDayHardSign,
    SigningDayShellLauncher,
    ReviewInjury,
    MedicalRedshirtCandidate,
    TeamDisciplinePending,
    PreseasonRequired,
    PlayScheduledGame,
    ReplayStagedWeekGame,
    CoachSundaySkillPoints,
    RecruitingWatchlistOffer,
    RecruitingWatchlistReEngage,
    RecruitingTop60Movement,
    RecruitingRivalCommit,
    PromiseAtRisk,
    CarouselLeagueSim,
    CarouselUserApproval,
    WeekOneProDay,
    WeekOneTransferRetention,
    SeasonReviewAcknowledge,
    OffseasonRosterLock,
    OffseasonConferenceOffer,
    SummerWorkoutReportReview,
    FundraisingGala,
    FundraisingAlumniGame,
    FundraisingDonorDrive,
    FundraisingNamingRights,
    FundraisingFacilityUpgrades,
}

data class DashboardTask(
    val id: DashboardTaskId,
    val kind: DashboardTaskKind,
    val deepLink: DashboardDeepLink,
    /** Optional format args for UI copy (e.g. points count, week index). */
    val formatArgs: List<Int> = emptyList(),
    val stringFormatArgs: List<String> = emptyList(),
    val preseasonTaskId: PreseasonTaskId? = null,
    /** Disambiguates repeated task ids in one week (e.g. multiple games — [MEGA-FRAME-106]). */
    val dismissScopeKey: String? = null,
)

object DashboardAdvanceGate {
    fun blocksWeekAdvance(tasks: List<DashboardTask>): Boolean =
        tasks.any { it.kind == DashboardTaskKind.Required }
}
