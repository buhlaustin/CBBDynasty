package dev.novux.dynasty.cbb.core.discipline

import dev.novux.dynasty.cbb.core.balance.BalanceConductTemplate
import dev.novux.dynasty.cbb.core.balance.BalanceDisciplineTuning
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

object DisciplineCalendarPolicy {
    private val activePhases = setOf(
        CalendarPhase.RegularSeason,
        CalendarPhase.ConferenceTournament,
        CalendarPhase.Postseason,
    )

    fun rollsOnWeekAdvance(phase: CalendarPhase): Boolean = phase in activePhases
}

object DisciplineOccurrenceEngine {
    fun rollAfterWeekAdvance(
        phase: CalendarPhase,
        rsWeekIndex: Int,
        players: List<RosterPlayer>,
        seasonState: TeamDisciplineSeasonState,
        tuning: BalanceDisciplineTuning,
        rollSeed: Long,
    ): PendingDisciplineIncident? {
        if (seasonState.pending != null) return null
        if (!DisciplineCalendarPolicy.rollsOnWeekAdvance(phase)) return null
        if (seasonState.eventsResolved >= tuning.occurrence.maxPerSeason) return null
        val baseChance = tuning.occurrence.baseChancePerTeamWeek
        val draw = (rollSeed.absoluteValue % 10_000) / 10_000.0
        if (draw >= baseChance) return null
        val academic = shouldRollAcademic(rsWeekIndex, tuning, rollSeed + 3)
        return if (academic) {
            rollAcademic(rsWeekIndex, players, tuning, rollSeed)
        } else {
            rollConduct(rsWeekIndex, players, tuning.conductTemplates, rollSeed)
        }
    }

    private fun shouldRollAcademic(
        rsWeekIndex: Int,
        tuning: BalanceDisciplineTuning,
        seed: Long,
    ): Boolean {
        val inWindow = rsWeekIndex in tuning.occurrence.academicWindowWeeks
        if (inWindow) return (seed.absoluteValue % 100) < 55
        return (seed.absoluteValue % 10_000) / 10_000.0 < tuning.occurrence.academicOffWindowChanceMultiplier
    }

    private fun rollAcademic(
        rsWeekIndex: Int,
        players: List<RosterPlayer>,
        tuning: BalanceDisciplineTuning,
        seed: Long,
    ): PendingDisciplineIncident? {
        val candidates = players.filter { player ->
            !player.academicHold &&
                player.classYear != PlayerClassYear.Freshman &&
                player.rsGamesPlayedSeason >= 0
        }
        val player = pickWeightedMinutes(candidates, seed) ?: return null
        val gpa = (1.05 + (seed.absoluteValue % 110) / 100.0).coerceAtMost(tuning.academic.gpaRequired - 0.01)
        val headline = "Academic eligibility — ${player.displayLabel()}"
        val body = "${player.displayLabel()}'s GPA is ${"%.2f".format(gpa)}, under the " +
            "${"%.1f".format(tuning.academic.gpaRequired)} eligibility requirement."
        return PendingDisciplineIncident(
            playerId = player.playerId,
            playerDisplayName = player.displayLabel(),
            kind = DisciplineIncidentKind.Academic,
            headline = headline,
            flavorBody = body,
            gpaCurrent = gpa,
            recordedAtRsWeek = rsWeekIndex,
        )
    }

    private fun rollConduct(
        rsWeekIndex: Int,
        players: List<RosterPlayer>,
        templates: List<BalanceConductTemplate>,
        seed: Long,
    ): PendingDisciplineIncident? {
        val template = pickTemplate(templates, seed) ?: return null
        val candidates = players.filter { it.rsGamesPlayedSeason >= 5 }
        val player = pickWeightedMinutes(candidates, seed + 7) ?: return null
        val label = player.displayLabel()
        return PendingDisciplineIncident(
            playerId = player.playerId,
            playerDisplayName = label,
            kind = DisciplineIncidentKind.Conduct,
            headline = DisciplineConductFlavorPolicy.headline(template),
            flavorBody = DisciplineConductFlavorPolicy.flavorBody(template.id, label),
            conductTemplateId = template.id,
            recordedAtRsWeek = rsWeekIndex,
        )
    }

    private fun pickTemplate(templates: List<BalanceConductTemplate>, seed: Long): BalanceConductTemplate? {
        if (templates.isEmpty()) return null
        val total = templates.sumOf { it.weight }
        var roll = (seed.absoluteValue % 10_000) / 10_000.0 * total
        for (t in templates) {
            roll -= t.weight
            if (roll <= 0) return t
        }
        return templates.last()
    }

    private fun pickWeightedMinutes(players: List<RosterPlayer>, seed: Long): RosterPlayer? {
        if (players.isEmpty()) return null
        val weights = players.map { (if (it.isStarter) 2.0 else 1.0) * (it.ovr / 80.0) }
        val total = weights.sum()
        var roll = (seed.absoluteValue % 10_000) / 10_000.0 * total
        players.forEachIndexed { index, player ->
            roll -= weights[index]
            if (roll <= 0) return player
        }
        return players.last()
    }

    private val Long.absoluteValue: Long get() = if (this < 0) -this else this
}
