package dev.novux.dynasty.cbb.core.qa

/**
 * Post-confirm week advance pipeline ([MEGA-FRAME-114]).
 *
 * Dialog latency is addressed separately via [SimWeekAdvancePreDialogPath] ([MEGA-QA-008], [MEGA-FRAME-093]).
 * After the user confirms, [SaveSlotRepository.advanceDynastyWeek] runs these stages in order.
 */
object DynastyWeekAdvancePipelineStages {
    const val FRAME_ID: String = "MEGA-FRAME-114"

    const val REPOSITORY_SOURCE: String =
        "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/save/SaveSlotRepository.kt"

    val confirmedAdvanceStageMarkers: List<String> = listOf(
        "runLeagueWeekIfActive",
        "weekCheckpointRepository.advanceWeek",
        "leagueWeekBatchRepository.afterWeekAdvance",
        "tickInjuriesForWeekAdvance",
        "disciplineRepository.afterWeekAdvance",
        "tickFatigueForWeekAdvance",
        "chemistryRepository.afterWeekAdvance",
        "coachSkillPointRepository.ensureSundayGrant",
        "loadSlot",
        "recruitingEconomyRepository.onCalendarWeekAdvanced",
        "dashboardNotificationWeekAdvanceAppender.appendAfterWeekAdvance",
    )
}