package dev.novux.dynasty.cbb.core.statistics

/**
 * Statistics hub route uses live leaderboards (not a static HubScaffold placeholder)
 * ([MEGA-SHELL-096]).
 */
object StatisticsHubLiveContentPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-096"

    fun isLiveHubRoute(): Boolean = true

    fun showLeaderboardUi(snapshot: StatisticsHubSnapshot): Boolean =
        StatisticsHubFramePolicy.showLiveLeaderboards(snapshot)
}