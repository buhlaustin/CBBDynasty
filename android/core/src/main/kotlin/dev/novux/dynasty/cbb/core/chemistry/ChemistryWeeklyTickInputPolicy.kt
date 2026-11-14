package dev.novux.dynasty.cbb.core.chemistry

import dev.novux.dynasty.cbb.core.coach.CoachArchetype
import dev.novux.dynasty.cbb.core.player.loyalty.PromiseStatus
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** Week-advance chemistry tick inputs beyond win/loss ([MEGA-SHELL-078]). */
object ChemistryWeeklyTickInputPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-078"

    const val ROSTER_MASS_CHANGE_THRESHOLD: Int = 3

    fun build(
        players: List<RosterPlayer>,
        headCoachArchetype: CoachArchetype,
        rosterSizeBeforeWeek: Int,
        teamPlayedGame: Boolean,
        userWonGame: Boolean?,
    ): ChemistryWeeklyTickInput =
        ChemistryWeeklyTickInput(
            userWonGame = if (teamPlayedGame) userWonGame else null,
            promiseBrokenThisSeason = players.any { it.promiseStatusWire == PromiseStatus.Broken.wire },
            rosterMassChangeThisWeek = (rosterSizeBeforeWeek - players.size) >= ROSTER_MASS_CHANGE_THRESHOLD,
            charismaBonus = charismaBonus(headCoachArchetype),
        )

    fun charismaBonus(archetype: CoachArchetype): Int = when (archetype) {
        CoachArchetype.Recruiter, CoachArchetype.ProgramBuilder -> 1
        else -> 0
    }
}