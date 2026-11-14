package dev.novux.dynasty.cbb.core.coach.staff

/** User-school staff Manage actions ([MEGA-COACH-054]). */
object StaffManagePolicy {
    const val QA_ID: String = "MEGA-COACH-054"

    const val MIN_ASSISTANT_COUNT: Int = 2

    val MANAGEABLE_KINDS: Set<StaffKind> = setOf(StaffKind.CoachTrack, StaffKind.Support)
}
