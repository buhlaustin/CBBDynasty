package dev.novux.dynasty.cbb.core.lineup

/** Team rotation minute budget vs regulation cap (MEGA-SIM-138). */
object RotationMinutesBudget {
    enum class Status {
        Under,
        Exact,
        Over,
    }

    fun status(
        totalMinutes: Int,
        cap: Int = RotationMinutesAllocator.TEAM_MINUTES_REGULATION,
    ): Status =
        when {
            totalMinutes < cap -> Status.Under
            totalMinutes > cap -> Status.Over
            else -> Status.Exact
        }

    fun progress(totalMinutes: Int, cap: Int = RotationMinutesAllocator.TEAM_MINUTES_REGULATION): Float =
        if (cap <= 0) 0f else (totalMinutes.toFloat() / cap).coerceIn(0f, 1.25f)
}
