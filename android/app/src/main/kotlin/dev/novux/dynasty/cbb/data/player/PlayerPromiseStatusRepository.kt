package dev.novux.dynasty.cbb.data.player

import dev.novux.dynasty.cbb.core.player.loyalty.PlayerPromiseInSeasonRefreshEngine
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository

/** Persists in-season promise status after week checkpoints ([MEGA-COACH-170]). */
class PlayerPromiseStatusRepository(
    private val dynastyRosterRepository: DynastyRosterRepository,
    private val playerStatsRepository: PlayerStatsRepository,
) {
    suspend fun refreshInSeasonAfterWeek(dynastyId: String, seasonIndex: Int) {
        val players = dynastyRosterRepository.listDynastyPlayers(dynastyId)
        players.forEach { player ->
            val mpg = playerStatsRepository.seasonMinutesPerGame(dynastyId, seasonIndex, player)
            val refreshed = PlayerPromiseInSeasonRefreshEngine.refresh(player, mpg)
            if (refreshed.loyalty != player.loyalty) {
                dynastyRosterRepository.upsertPlayer(refreshed.toRosterPlayer())
            }
        }
    }
}
