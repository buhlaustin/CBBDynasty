package dev.novux.dynasty.cbb.core.news

/**
 * February bracketology cadence during regular season ([planning/game-design.md#media-systems], [MEGA-GDD-130]).
 */
object BracketologyNewsPolicy {
    const val MEGA_ID: String = "MEGA-GDD-130"

    /** Late RS weeks mapped to February bracketology window (32-game schedule). */
    val februaryRegularSeasonWeeks: IntRange = 20..28

    const val LOCK_LINE_COUNT: Int = 4
    const val BUBBLE_LINE_COUNT: Int = 3

    fun shouldPublish(rsWeekIndex: Int): Boolean = rsWeekIndex in februaryRegularSeasonWeeks
}
