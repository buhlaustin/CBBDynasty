package dev.novux.dynasty.cbb.core.save

import dev.novux.dynasty.cbb.core.calendar.SeasonMembershipYear

/** New dynasty starts at membership year 1 → OS week 1 ([MEGA-GDD-055]). */
object NewGameWizardSeasonPolicy {
    const val MEGA_ID: String = "MEGA-GDD-055"

    const val startingSeasonIndex: Int = 1

    fun startingMembershipYearLabel(): String = SeasonMembershipYear.label(startingSeasonIndex)
}