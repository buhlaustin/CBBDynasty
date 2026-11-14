package dev.novux.dynasty.cbb.core.school

/** Opponent school hub is read-only overview ([MEGA-GDD-073]). */
object SchoolHubViewOnlyPolicy {
    const val MEGA_ID: String = "MEGA-GDD-073"

    fun showViewOnlyBanner(overview: SchoolHubOverview): Boolean = !overview.isUserSchool
}