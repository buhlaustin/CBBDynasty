package dev.novux.dynasty.cbb.core.statistics

/** Statistics hamburger hub uses live leaderboards when stats exist ([MEGA-GDD-053]). */
object StatisticsHubFramePolicy {
    const val MEGA_ID: String = "MEGA-GDD-053"

    fun showLiveLeaderboards(snapshot: StatisticsHubSnapshot): Boolean = snapshot.hasStats
}