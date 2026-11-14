package dev.novux.dynasty.cbb.core.coach.card

import dev.novux.dynasty.cbb.core.coach.staff.SchoolStaffRoster

/** Gates user-only coach UI (job security, edit, resign) vs opponent cards ([MEGA-COACH-040]). */
object CoachCardUserGate {
    fun isUserHeadCoach(card: HeadCoachCard, staff: SchoolStaffRoster?): Boolean {
        val head = staff?.headCoach ?: return false
        return head.displayName == card.displayName &&
            head.schoolDisplayName == card.schoolDisplayName
    }

    fun isUserAssistant(card: AssistantCoachCard, staff: SchoolStaffRoster?): Boolean {
        if (staff == null) return false
        return staff.assistants.any {
            it.staffId == card.staffId && it.displayName == card.displayName
        }
    }
}
