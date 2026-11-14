package dev.novux.dynasty.cbb.core.league

import dev.novux.dynasty.cbb.core.balance.BalanceInjuryOccurrence
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.injuries.InjuryBankRow
import dev.novux.dynasty.cbb.core.injuries.InjuryOccurrenceEngine
import dev.novux.dynasty.cbb.core.sim.GameSimResult

/** AI injury bank + lineup adjust hooks for league batch week ([planning/injuries.md]). */
object AiLeagueWeekOps {
    data class InjuryRollParams(
        val phase: CalendarPhase,
        val bank: List<InjuryBankRow>,
        val occurrence: BalanceInjuryOccurrence,
        val medicalStaffGrade: Int = 70,
    )

    data class GameInjuryRoll(
        val homeTeamId: String,
        val awayTeamId: String,
        val homeEvents: List<GameSimInjuryEvent>,
        val awayEvents: List<GameSimInjuryEvent>,
    ) {
        val totalEvents: Int get() = homeEvents.size + awayEvents.size
    }

    fun rollInjuriesForCompletedGame(
        homeTeamId: String,
        awayTeamId: String,
        sim: GameSimResult,
        gameSeed: Long,
        homePrestige: Int,
        awayPrestige: Int,
        params: InjuryRollParams,
    ): GameInjuryRoll {
        val homeRoster = AiLeagueSyntheticRoster.playersForTeam(homeTeamId, homePrestige)
        val awayRoster = AiLeagueSyntheticRoster.playersForTeam(awayTeamId, awayPrestige)
        val homeMinutes = AiLeagueSyntheticRoster.minutesByPlayer(homeRoster, sim.homeBox.possessions)
        val awayMinutes = AiLeagueSyntheticRoster.minutesByPlayer(awayRoster, sim.awayBox.possessions)
        val home = InjuryOccurrenceEngine.rollAfterGame(
            phase = params.phase,
            minutesByPlayerId = homeMinutes,
            roster = homeRoster,
            bank = params.bank,
            tuning = params.occurrence,
            medicalStaffGrade = params.medicalStaffGrade,
            gameSeed = gameSeed xor homeTeamId.hashCode().toLong(),
            existingInjuryCount = sim.homeInjuries.size,
        ).injuries
        val away = InjuryOccurrenceEngine.rollAfterGame(
            phase = params.phase,
            minutesByPlayerId = awayMinutes,
            roster = awayRoster,
            bank = params.bank,
            tuning = params.occurrence,
            medicalStaffGrade = params.medicalStaffGrade,
            gameSeed = gameSeed xor awayTeamId.hashCode().toLong(),
            existingInjuryCount = sim.awayInjuries.size,
        ).injuries
        return GameInjuryRoll(homeTeamId, awayTeamId, home, away)
    }

    /** @deprecated Random injury counts — use [rollInjuriesForCompletedGame]. */
    fun rollInjuriesForGames(gameCount: Int, seed: Long): Int {
        if (gameCount <= 0) return 0
        val random = kotlin.random.Random(seed)
        var injuries = 0
        repeat(gameCount) { index ->
            val roll = random.nextDouble()
            if (roll < 0.06) injuries++
            if (roll < 0.012 && index % 3 == 0) injuries++
        }
        return injuries
    }

    fun lineupAdjustTeamsAffected(rolls: Collection<GameInjuryRoll>): Int {
        val teams = mutableSetOf<String>()
        rolls.forEach { roll ->
            if (roll.homeEvents.isNotEmpty()) teams += roll.homeTeamId
            if (roll.awayEvents.isNotEmpty()) teams += roll.awayTeamId
        }
        return teams.size
    }

    fun lineupAdjustTeamsAffected(injuryRolls: Int): Int = injuryRolls
}
