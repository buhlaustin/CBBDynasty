package dev.novux.dynasty.cbb.data.selection

import dev.novux.dynasty.cbb.core.balance.selectionTuning
import dev.novux.dynasty.cbb.core.selection.ConferenceChampionResolver
import dev.novux.dynasty.cbb.core.selection.NcbaConferenceAutoBidPolicy
import dev.novux.dynasty.cbb.core.selection.SelectionSundayEngine
import dev.novux.dynasty.cbb.core.selection.SelectionSundayInput
import dev.novux.dynasty.cbb.core.selection.SelectionSundaySnapshot
import dev.novux.dynasty.cbb.data.balance.BalanceRulesetRepository
import dev.novux.dynasty.cbb.data.league.LeagueWeekBatchRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository

class SelectionSundayRepository(
    private val packRepository: PackRepository,
    private val leagueWeekBatchRepository: LeagueWeekBatchRepository,
    private val balanceRulesetRepository: BalanceRulesetRepository,
) {
    suspend fun load(dynastyId: String, seasonIndex: Int): SelectionSundaySnapshot? =
        leagueWeekBatchRepository.loadStandings(dynastyId, seasonIndex)?.selectionSunday

    suspend fun computeAndPersist(dynastyId: String, seasonIndex: Int): SelectionSundaySnapshot? {
        val standings = leagueWeekBatchRepository.loadStandings(dynastyId, seasonIndex) ?: return null
        if (standings.netRankByTeam.isEmpty()) return null
        val snapshot = buildSnapshot(seasonIndex, standings)
        leagueWeekBatchRepository.persistStandings(dynastyId, standings.copy(selectionSunday = snapshot))
        return snapshot
    }

    private suspend fun buildSnapshot(
        seasonIndex: Int,
        standings: dev.novux.dynasty.cbb.core.league.LeagueStandingsState,
    ): SelectionSundaySnapshot {
        val index = packRepository.activeSession().index
        val teams = index.allTeamDisplays()
        val tuning = balanceRulesetRepository.activeRuleset().selectionTuning()
        val net = standings.netRankByTeam
        return SelectionSundayEngine.run(
            SelectionSundayInput(
                seasonIndex = seasonIndex,
                netRankByTeam = net,
                postseasonEligible = teams.associate { it.teamId to it.postseasonEligible },
                heritageEligible = teams.associate { it.teamId to true },
                conferenceChampions = standings.conferenceTournaments?.let {
                    ConferenceChampionResolver.ncbaAutoBidsFromTournaments(it)
                } ?: NcbaConferenceAutoBidPolicy.fromStandings(
                    teams = teams,
                    records = standings.records,
                    seasonResults = standings.seasonResults,
                    tiebreakersByConference = { confId ->
                        index.conference(confId)?.standingsTiebreakers ?: emptyList()
                    },
                    netRankByTeam = net,
                ),
                programPrestige = teams.associate { it.teamId to it.programPrestige },
                records = standings.records,
                results = standings.seasonResults,
                tuning = tuning,
            ),
        )
    }
}
