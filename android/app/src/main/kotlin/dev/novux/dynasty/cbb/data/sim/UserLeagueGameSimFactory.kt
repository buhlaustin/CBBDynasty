package dev.novux.dynasty.cbb.data.sim

import dev.novux.dynasty.cbb.core.advance.UserLeagueFastSimBundle
import dev.novux.dynasty.cbb.core.advance.UserLeagueGameIds
import dev.novux.dynasty.cbb.core.advance.UserLeagueGameSimContext
import dev.novux.dynasty.cbb.core.dashboard.InSeasonWeekGames
import dev.novux.dynasty.cbb.core.injuries.InjuryOccurrenceEngine
import dev.novux.dynasty.cbb.core.lineup.DefaultStarterAlgorithm
import dev.novux.dynasty.cbb.core.lineup.RotationMinutesBalancer
import dev.novux.dynasty.cbb.core.league.TeamSimRatings
import dev.novux.dynasty.cbb.core.balance.injuryTuning
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.core.sim.GameLineupPrep
import dev.novux.dynasty.cbb.core.sim.GameSimInjuryRollInput
import dev.novux.dynasty.cbb.core.sim.GameSimTeamInput
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.strategy.DefensiveAssignmentPolicy
import dev.novux.dynasty.cbb.core.strategy.GameplanInput
import dev.novux.dynasty.cbb.core.strategy.GameplanUsageNormalizer
import dev.novux.dynasty.cbb.data.balance.BalanceRulesetRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonScheduleRepository
import dev.novux.dynasty.cbb.data.chemistry.ChemistryRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import dev.novux.dynasty.cbb.data.strategy.DynastyStrategyPrepRepository
import dev.novux.dynasty.cbb.data.sim.SimTuningRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase

/** Builds possession-sim contexts for unplayed user league games on week advance. */
class UserLeagueGameSimFactory(
    private val database: DynastyDatabase,
    private val packRepository: PackRepository,
    private val balanceRulesetRepository: BalanceRulesetRepository,
    private val simTuningRepository: SimTuningRepository,
    private val preseasonScheduleRepository: PreseasonScheduleRepository,
    private val dynastyRosterRepository: DynastyRosterRepository,
    private val strategyPrepRepository: DynastyStrategyPrepRepository,
    private val chemistryRepository: ChemistryRepository,
) {
    suspend fun buildFastSimBundle(
        loaded: LoadedDynasty,
        unplayedCount: Int,
        dynastySeed: Long,
    ): UserLeagueFastSimBundle? {
        if (unplayedCount <= 0) return null
        val position = loaded.weekCheckpoint.activePosition
        val plan = preseasonScheduleRepository.loadPlan(loaded.dynastyId, loaded.metadata.seasonIndex)
        val exhibitionCount = preseasonScheduleRepository.loadExhibitionCount(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
        val weekGames = InSeasonWeekGames.forPosition(position, plan, exhibitionCount)
        if (weekGames.isEmpty()) return null
        val tuning = simTuningRepository.activeTuning()
        val ruleset = balanceRulesetRepository.activeRuleset()
        val injuryTuning = ruleset.injuryTuning()
        val bank = InjuryOccurrenceEngine.bankFromBalance(ruleset.injuries.bank)
        val coach = database.dynastyCoachDao().getByDynastyId(loaded.dynastyId)
        val userTeamId = coach?.userTeamId ?: "user-${loaded.dynastyId}"
        val players = dynastyRosterRepository.listPlayers(loaded.dynastyId)
        val balanceFatigue = ruleset.fatigue
        val starters = DefaultStarterAlgorithm.pickStarters(
            players,
            starterBlockStamina = balanceFatigue.starterBlockThreshold,
        )
        val rotation = RotationMinutesBalancer.baseline(players, starters)
        val gameplan = defaultGameplan(starters)
        val (offensive, defensive) = strategyPrepRepository.resolveForLeagueSim(loaded.dynastyId)
        val teamChem = chemistryRepository.loadState(loaded.dynastyId, loaded.metadata.seasonIndex).teamChemistry
        val chemistryTuning = ruleset.chemistry
        val homeRoster = if (players.isNotEmpty()) {
            GameLineupPrep.build(userTeamId, players, starters, rotation)
        } else {
            null
        }
        val userPrestige = packRepository.activeSession().index.team(userTeamId)?.programPrestige ?: 50
        val (userOff, userDef) = TeamSimRatings.fromProgramPrestige(userPrestige)
        val contexts = weekGames.take(unplayedCount).mapIndexed { index, game ->
            val oppPrestige = packRepository.activeSession().index.team(game.opponentTeamId)?.programPrestige ?: 50
            val (oppOff, oppDef) = TeamSimRatings.fromProgramPrestige(oppPrestige)
            val userTeamIsHome = game.homeGame
            val homeTeam = if (userTeamIsHome) {
                GameSimTeamInput(userTeamId, userOff, userDef, offensiveScheme = offensive, defensiveScheme = defensive)
            } else {
                GameSimTeamInput(
                    game.opponentTeamId,
                    oppOff,
                    oppDef,
                    offensiveScheme = dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset.Balanced,
                    defensiveScheme = dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset.ManToMan,
                )
            }
            val awayTeam = if (userTeamIsHome) {
                GameSimTeamInput(game.opponentTeamId, oppOff, oppDef)
            } else {
                GameSimTeamInput(userTeamId, userOff, userDef, offensiveScheme = offensive, defensiveScheme = defensive)
            }
            val seed = dynastySeed xor position.hashCode().toLong() xor index.toLong()
            UserLeagueGameSimContext(
                gameId = UserLeagueGameIds.forPosition(position, index),
                userTeamIsHome = userTeamIsHome,
                tuning = tuning,
                seed = seed,
                home = homeTeam,
                away = awayTeam,
                homeRoster = if (userTeamIsHome) homeRoster else null,
                awayRoster = if (!userTeamIsHome) homeRoster else null,
                homeGameplan = if (userTeamIsHome) gameplan else null,
                injuryRoll = GameSimInjuryRollInput(
                    phase = position.phase,
                    bank = bank,
                    occurrence = injuryTuning.occurrence,
                    medicalStaffGrade = 70,
                    homeRosterPlayers = if (userTeamIsHome) players else emptyList(),
                    awayRosterPlayers = if (userTeamIsHome) emptyList() else players,
                ),
                userTeamChemistry = teamChem,
                chemistryTuning = chemistryTuning,
            )
        }
        return UserLeagueFastSimBundle(contexts)
    }

    suspend fun resolveSchemesForFatigueTick(
        dynastyId: String,
    ): Pair<
        dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset,
        dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset,
        > = strategyPrepRepository.resolveForLeagueSim(dynastyId)

    private fun defaultGameplan(
        starters: Map<dev.novux.dynasty.cbb.core.roster.PlayerPosition, String>,
    ): GameplanInput {
        if (starters.isEmpty()) return GameplanInput()
        return GameplanInput(
            usageWeights = GameplanUsageNormalizer.defaultForStarters(starters.values),
            defensiveAssignments = DefensiveAssignmentPolicy.defaultAssignments(starters),
        )
    }
}
