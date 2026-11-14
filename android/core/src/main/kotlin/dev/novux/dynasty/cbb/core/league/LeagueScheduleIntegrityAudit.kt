package dev.novux.dynasty.cbb.core.league

import dev.novux.dynasty.cbb.core.preseason.tournament.RegularSeasonSlotBudget

/**
 * RS cap, mutual symmetry, and single-game-per-week checks ([MEGA-NOTES-047], [MEGA-WORLD-062]–[064]).
 */
object LeagueScheduleIntegrityAudit {
    const val MEGA_ID: String = "MEGA-NOTES-047"

    data class Report(
        val capViolations: Int = 0,
        val asymmetricPairs: Int = 0,
        val doubleBookedTeamWeeks: Int = 0,
    ) {
        val isClean: Boolean = capViolations == 0 && asymmetricPairs == 0 && doubleBookedTeamWeeks == 0
    }

    fun audit(season: LeagueScheduleSeason): Report {
        var capViolations = 0
        var asymmetric = 0
        var doubleBooked = 0

        val byTeam = season.schedules.associate { schedule ->
            schedule.teamId to schedule.games.groupBy { it.opponentTeamId to it.rsWeekIndex }
        }

        season.schedules.forEach { teamSchedule ->
            if (teamSchedule.rsGameCount > RegularSeasonSlotBudget.RS_CAP) capViolations++
            val weeks = teamSchedule.games.map { it.rsWeekIndex }
            if (weeks.size != weeks.toSet().size) doubleBooked++
            teamSchedule.games.forEach { game ->
                val oppGames = byTeam[game.opponentTeamId]?.get(teamSchedule.teamId to game.rsWeekIndex)
                val mirror = oppGames?.singleOrNull()
                if (mirror == null || mirror.homeGame == game.homeGame) {
                    asymmetric++
                }
            }
        }
        asymmetric /= 2

        return Report(capViolations, asymmetric, doubleBooked)
    }
}
