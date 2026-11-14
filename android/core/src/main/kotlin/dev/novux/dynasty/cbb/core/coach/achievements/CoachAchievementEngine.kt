package dev.novux.dynasty.cbb.core.coach.achievements

import dev.novux.dynasty.cbb.core.brackets.NationalBracketSnapshot
import dev.novux.dynasty.cbb.core.brackets.NcbaMainBracketSnapshot
import dev.novux.dynasty.cbb.core.brackets.NcbaOpeningRoundSnapshot
import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState

object CoachAchievementEngine {
    fun empty(dynastyId: String): CoachAchievementLedger = CoachAchievementLedger(dynastyId = dynastyId)

    fun snapshot(
        ledger: CoachAchievementLedger,
        standings: LeagueStandingsState?,
        userTeamId: String,
    ): HeadCoachAchievements {
        val totals = displayTotals(ledger, standings, userTeamId)
        return HeadCoachAchievements.fromLedger(totals)
    }

    fun displayTotals(
        ledger: CoachAchievementLedger,
        standings: LeagueStandingsState?,
        userTeamId: String,
    ): CoachAchievementLedger {
        if (standings == null || standings.seasonIndex in ledger.rolledSeasons) return ledger
        val delta = seasonDelta(standings, userTeamId)
        return ledger + delta.copy(dynastyId = ledger.dynastyId, rolledSeasons = emptySet())
    }

    fun finalizeSeason(
        ledger: CoachAchievementLedger,
        standings: LeagueStandingsState,
        userTeamId: String,
    ): CoachAchievementLedger {
        if (standings.seasonIndex in ledger.rolledSeasons) return ledger
        val delta = seasonDelta(standings, userTeamId)
        return (ledger + delta.copy(dynastyId = ledger.dynastyId, rolledSeasons = emptySet())).copy(
            rolledSeasons = ledger.rolledSeasons + standings.seasonIndex,
        )
    }

    fun seasonDelta(
        standings: LeagueStandingsState,
        userTeamId: String,
    ): CoachAchievementLedger {
        val rec = standings.recordFor(userTeamId)
        var delta = CoachAchievementLedger(
            dynastyId = "",
            careerWins = rec.wins,
            careerLosses = rec.losses,
        )
        val selection = standings.selectionSunday
        if (selection?.ncbaField?.any { it.teamId == userTeamId } == true) {
            delta = delta.copy(ncbaAppearances = 1)
        }
        standings.ncbaOpeningRound?.let { opening ->
            val (w, l) = bracketRecord(userTeamId, opening.gameResults)
            delta = delta.copy(ncbaTourneyWins = delta.ncbaTourneyWins + w, ncbaTourneyLosses = delta.ncbaTourneyLosses + l)
        }
        standings.ncbaMainBracket?.let { main ->
            val (w, l) = bracketRecord(userTeamId, main.gameResults)
            delta = delta.copy(ncbaTourneyWins = delta.ncbaTourneyWins + w, ncbaTourneyLosses = delta.ncbaTourneyLosses + l)
            if (main.championTeamId == userTeamId) delta = delta.copy(nationalTitles = 1)
            if (main.regionalChampions.values.contains(userTeamId)) delta = delta.copy(finalFours = 1)
        }
        standings.nciBracket?.let { nci ->
            val (w, l) = bracketRecord(userTeamId, nci.gameResults)
            delta = delta.copy(nciWins = w, nciLosses = l)
            if (nci.championTeamId == userTeamId) delta = delta.copy(nciTitles = 1)
        }
        standings.heritageBracket?.let { heritage ->
            val (w, l) = bracketRecord(userTeamId, heritage.gameResults)
            delta = delta.copy(heritageWins = w, heritageLosses = l)
            if (heritage.championTeamId == userTeamId) delta = delta.copy(heritageTitles = 1)
        }
        standings.conferenceTournaments?.resultsByConference?.values?.forEach { result ->
            if (result.championTeamId == userTeamId) {
                delta = delta.copy(confTourneyTitles = delta.confTourneyTitles + 1)
            }
        }
        return delta
    }

    private fun bracketRecord(
        userTeamId: String,
        results: List<LeagueGameResult>,
    ): Pair<Int, Int> {
        var wins = 0
        var losses = 0
        results.forEach { game ->
            if (userTeamId != game.homeTeamId && userTeamId != game.awayTeamId) return@forEach
            val userWon = when (userTeamId) {
                game.homeTeamId -> game.homeScore > game.awayScore
                else -> game.awayScore > game.homeScore
            }
            if (userWon) wins++ else losses++
        }
        return wins to losses
    }
}
