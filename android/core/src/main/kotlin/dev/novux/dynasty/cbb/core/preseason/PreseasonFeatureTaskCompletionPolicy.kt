package dev.novux.dynasty.cbb.core.preseason

/** Preseason checklist rows wired to feature flows — no bare "Complete" shortcut. */
object PreseasonFeatureTaskCompletionPolicy {
    val noGenericCompleteButton: Set<PreseasonTaskId> = setOf(
        PreseasonTaskId.PreseasonWorkouts,
        PreseasonTaskId.CoachUpgrades,
        PreseasonTaskId.RankingsReview,
        PreseasonTaskId.PreseasonReport,
        PreseasonTaskId.IncomingClassSeed,
        PreseasonTaskId.TournamentInvites,
        PreseasonTaskId.ScheduleExhibitions,
        PreseasonTaskId.ScheduleRegularSeason,
    )

    fun allowsGenericCompleteButton(taskId: PreseasonTaskId): Boolean =
        taskId !in noGenericCompleteButton

}
