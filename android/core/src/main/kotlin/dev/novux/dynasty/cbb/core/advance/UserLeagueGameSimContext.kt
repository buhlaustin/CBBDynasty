package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.balance.BalanceChemistry
import dev.novux.dynasty.cbb.core.sim.GameSimInput
import dev.novux.dynasty.cbb.core.sim.GameSimInjuryRollInput
import dev.novux.dynasty.cbb.core.sim.GameSimResult
import dev.novux.dynasty.cbb.core.sim.GameSimRosterInput
import dev.novux.dynasty.cbb.core.sim.GameSimTeamInput
import dev.novux.dynasty.cbb.core.sim.PossessionSimEngine
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.strategy.GameplanInput

data class UserLeagueGameSimContext(
    val gameId: String,
    val userTeamIsHome: Boolean,
    val tuning: SimTuning,
    val seed: Long,
    val home: GameSimTeamInput,
    val away: GameSimTeamInput,
    val homeRoster: GameSimRosterInput? = null,
    val awayRoster: GameSimRosterInput? = null,
    val homeGameplan: GameplanInput? = null,
    val injuryRoll: GameSimInjuryRollInput? = null,
    val userTeamChemistry: Int? = null,
    val chemistryTuning: BalanceChemistry? = null,
) {
    fun toGameSimInput(): GameSimInput = GameSimInput(
        home = home,
        away = away,
        tuning = tuning,
        seed = seed,
        homeRoster = homeRoster,
        awayRoster = awayRoster,
        homeGameplan = homeGameplan,
        homeTeamChemistry = if (userTeamIsHome) userTeamChemistry else null,
        awayTeamChemistry = if (!userTeamIsHome) userTeamChemistry else null,
        chemistryTuning = chemistryTuning,
    )

    fun simulate(): GameSimResult =
        PossessionSimEngine.simulate(toGameSimInput(), injuryRoll = injuryRoll)
}

data class UserLeagueFastSimBundle(
    val contexts: List<UserLeagueGameSimContext>,
)
