package dev.novux.dynasty.cbb.core.preseason

/** Eight-task preseason checklist progress ([MEGA-GDD-039]). */
object PreseasonTaskCompletenessPolicy {
    const val MEGA_ID: String = "MEGA-GDD-039"

    val requiredCount: Int = PreseasonTaskId.entries.size

    fun completedCount(progress: PreseasonTaskProgress): Int =
        PreseasonTaskId.entries.count { progress.isComplete(it) }

    fun incompleteTasks(progress: PreseasonTaskProgress): List<PreseasonTaskId> =
        PreseasonTaskId.entries.filterNot { progress.isComplete(it) }
}