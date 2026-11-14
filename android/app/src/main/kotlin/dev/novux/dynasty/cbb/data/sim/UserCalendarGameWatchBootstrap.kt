package dev.novux.dynasty.cbb.data.sim

import dev.novux.dynasty.cbb.core.advance.UserLeagueGameIds
import dev.novux.dynasty.cbb.core.balance.injuryTuning
import dev.novux.dynasty.cbb.core.dashboard.InSeasonWeekGames
import dev.novux.dynasty.cbb.core.injuries.InjuryOccurrenceEngine
import dev.novux.dynasty.cbb.core.lineup.DefaultStarterAlgorithm
import dev.novux.dynasty.cbb.core.lineup.RotationMinutesBalancer
import dev.novux.dynasty.cbb.core.league.TeamSimRatings
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.core.sim.GameLineupPrep
import dev.novux.dynasty.cbb.core.sim.GameSimInjuryRollInput
import dev.novux.dynasty.cbb.core.sim.SimHomeVenueType
import dev.novux.dynasty.cbb.core.strategy.DefensiveAssignmentPolicy
import dev.novux.dynasty.cbb.core.strategy.GameplanInput
import dev.novux.dynasty.cbb.core.strategy.GameplanUsageNormalizer
import dev.novux.dynasty.cbb.data.balance.BalanceRulesetRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonScheduleRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import dev.novux.dynasty.cbb.data.strategy.DynastyStrategyPrepRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase
import dev.novux.dynasty.cbb.ui.sim.GameWatchLaunch

/** Wires calendar user game into watch sim with prep, schemes, injuries ([MEGA-SIM-173]). */
class UserCalendarGameWatchBootstrap(
    private val database: DynastyDatabase,
    private val packRepository: PackRepository,
    private val balanceRulesetRepository: BalanceRulesetRepository,
    private val preseasonScheduleRepository: PreseasonScheduleRepository,
    private val dynastyRosterRepository: DynastyRosterRepository,
    private val strategyPrepRepository: DynastyStrategyPrepRepository,
) {
    suspend fun configure(loaded: LoadedDynasty): Boolean {
        val position = loaded.weekCheckpoint.activePosition
        val plan = preseasonScheduleRepository.loadPlan(loaded.dynastyId, loaded.metadata.seasonIndex)
            ?: return false
        val exhibitionCount = preseasonScheduleRepository.loadExhibitionCount(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
        val game = InSeasonWeekGames.forPosition(position, plan, exhibitionCount).firstOrNull()
            ?: return false
        val players = dynastyRosterRepository.listPlayers(loaded.dynastyId)
        if (players.isEmpty()) return false
        val ruleset = balanceRulesetRepository.activeRuleset()
        val fatigue = ruleset.fatigue
        val starters = DefaultStarterAlgorithm.pickStarters(players, fatigue.starterBlockThreshold)
        val rotation = RotationMinutesBalancer.baseline(players, starters)
        val (offensive, defensive) = strategyPrepRepository.resolveForLeagueSim(loaded.dynastyId)
        val gameplan = GameplanInput(
            usageWeights = GameplanUsageNormalizer.defaultForStarters(starters.values),
            defensiveAssignments = DefensiveAssignmentPolicy.defaultAssignments(starters),
        )
        val coach = database.dynastyCoachDao().getByDynastyId(loaded.dynastyId) ?: return false
        val userTeamId = coach.userTeamId
        val userName = packRepository.activeSession().index.team(userTeamId)?.displayName
            ?: loaded.summary.schoolDisplayName
            ?: "Home"
        val oppName = game.opponentDisplayName
        val (oppOff, oppDef) = TeamSimRatings.fromProgramPrestige(
            packRepository.activeSession().index.team(game.opponentTeamId)?.programPrestige ?: 55,
        )
        val (userOff, userDef) = TeamSimRatings.fromProgramPrestige(
            packRepository.activeSession().index.team(userTeamId)?.programPrestige ?: 55,
        )
        val userIsHome = game.homeGame
        val injuryTuning = ruleset.injuryTuning()
        val bank = InjuryOccurrenceEngine.bankFromBalance(ruleset.injuries.bank)
        GameWatchLaunch.userTeamIsHome = userIsHome
        GameWatchLaunch.homeVenue = if (userIsHome) SimHomeVenueType.Home else SimHomeVenueType.Road
        GameWatchLaunch.isScrimmage = false
        GameWatchLaunch.homeName = if (userIsHome) userName else oppName
        GameWatchLaunch.awayName = if (userIsHome) oppName else userName
        GameWatchLaunch.userOffensiveRating = userOff
        GameWatchLaunch.userDefensiveRating = userDef
        GameWatchLaunch.awayOffensiveRating = oppOff
        GameWatchLaunch.awayDefensiveRating = oppDef
        GameWatchLaunch.homeOffensiveScheme = offensive
        GameWatchLaunch.homeDefensiveScheme = defensive
        GameWatchLaunch.homeGameplan = gameplan
        GameWatchLaunch.homeRosterPlayers = players
        GameWatchLaunch.homeStarters = starters
        GameWatchLaunch.rotationMinutes = rotation
        GameWatchLaunch.homeSimRoster = GameLineupPrep.build(userTeamId, players, starters, rotation)
        GameWatchLaunch.simSeed = loaded.dynastyId.hashCode().toLong() xor position.hashCode().toLong()
        GameWatchLaunch.injuryRoll = GameSimInjuryRollInput(
            phase = position.phase,
            bank = bank,
            occurrence = injuryTuning.occurrence,
            medicalStaffGrade = 70,
            homeRosterPlayers = if (userIsHome) players else emptyList(),
            awayRosterPlayers = if (userIsHome) emptyList() else players,
        )
        GameWatchLaunch.activeGameId = UserLeagueGameIds.forPosition(position)
        return true
    }
}
