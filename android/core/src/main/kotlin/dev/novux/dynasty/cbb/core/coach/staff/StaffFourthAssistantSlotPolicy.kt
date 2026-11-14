package dev.novux.dynasty.cbb.core.coach.staff

/** Assistant slots: 3 default, 4th when program prestige ≥ 85 ([MEGA-GDD-036], [MEGA-GDD-123]). */
object StaffFourthAssistantSlotPolicy {
    const val MEGA_ID: String = "MEGA-GDD-036"
    const val FOURTH_SLOT_PRESTIGE_THRESHOLD: Int = 85

    fun maxAssistantSlots(programPrestige: Int): Int =
        if (programPrestige >= FOURTH_SLOT_PRESTIGE_THRESHOLD) 4 else 3

    fun fourthSlotUnlocked(programPrestige: Int): Boolean =
        programPrestige >= FOURTH_SLOT_PRESTIGE_THRESHOLD

    fun showFourthSlotPlaceholder(programPrestige: Int, currentAssistantCount: Int): Boolean =
        fourthSlotUnlocked(programPrestige) && currentAssistantCount < 4
}