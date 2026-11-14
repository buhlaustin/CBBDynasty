package dev.novux.dynasty.cbb.data.rankings

import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId
import dev.novux.dynasty.cbb.core.rankings.RankingsHubPublishPolicy
import dev.novux.dynasty.cbb.core.rankings.RankingsHubSnapshot
import dev.novux.dynasty.cbb.core.rankings.RankingsHubSnapshotFactory
import dev.novux.dynasty.cbb.data.league.LeagueWeekBatchRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonProgressRepository

class RankingsHubRepository(
    private val packRepository: PackRepository,
    private val leagueWeekBatchRepository: LeagueWeekBatchRepository,
    private val preseasonProgressRepository: PreseasonProgressRepository,
) {
    suspend fun load(dynastyId: String, seasonIndex: Int): RankingsHubSnapshot {
        val index = packRepository.activeSession().index
        val standings = leagueWeekBatchRepository.loadStandings(dynastyId, seasonIndex)
        val preseasonComplete = preseasonProgressRepository
            .load(dynastyId, seasonIndex)
            .isComplete(PreseasonTaskId.RankingsReview)
        val published = RankingsHubPublishPolicy.rankingsPublished(preseasonComplete, standings)
        return RankingsHubSnapshotFactory.build(index, standings, published)
    }

    suspend fun publishPreseasonRankings(dynastyId: String, seasonIndex: Int): RankingsHubSnapshot {
        preseasonProgressRepository.completeTask(dynastyId, seasonIndex, PreseasonTaskId.RankingsReview)
        return load(dynastyId, seasonIndex)
    }
}
