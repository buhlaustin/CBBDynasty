package dev.novux.dynasty.cbb.core.injuries

import dev.novux.dynasty.cbb.core.balance.BalanceMedicalRedshirtRules
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** Medical RS gates + copy inputs for player cards ([MEGA-GDD-023]). */
object MedicalRedshirtPolicy {
    const val MEGA_ID: String = "MEGA-GDD-023"
    data class Eligibility(
        val eligible: Boolean,
        val reason: String? = null,
    )

    fun evaluate(
        player: RosterPlayer,
        rsWeekIndex: Int,
        rules: BalanceMedicalRedshirtRules,
        countsTowardMedicalRedshirt: Boolean = true,
    ): Eligibility {
        if (!countsTowardMedicalRedshirt) return Eligibility(false, "exhibition_game")
        if (!player.injuryActive) return Eligibility(false, "not_injured")
        if (player.medicalRedshirtSeason) return Eligibility(false, "already_medical_rs")
        val severity = player.injurySeverityWire?.let(InjurySeverity::fromWire)
            ?: return Eligibility(false, "unknown_severity")
        if (severity.wire !in rules.qualifyingSeverities) {
            return Eligibility(false, "severity_not_qualifying")
        }
        if (rsWeekIndex >= rules.blockedFromRegularSeasonWeek) {
            return Eligibility(false, "after_week_gate")
        }
        if (player.rsGamesPlayedSeason > rules.maxGamesPlayed) {
            return Eligibility(false, "too_many_games_played")
        }
        if (player.rsGamesPlayedSeason < rules.maxGamesPlayed &&
            player.injuryOccurredAtRsGameIndex > rules.maxScheduleRsGameIndex
        ) {
            return Eligibility(false, "schedule_index_too_late")
        }
        return Eligibility(true)
    }

    fun apply(player: RosterPlayer): RosterPlayer =
        player.copy(
            medicalRedshirtSeason = true,
            injuryActive = false,
            injuryWeeksRemaining = 0,
            injuryLabel = "Medical redshirt",
            isStarter = false,
            starterSlotWire = null,
        )

    /** Copy for medical RS confirmation — [planning/injuries.md#ux-flow]. */
    fun confirmationDetail(player: RosterPlayer, rules: BalanceMedicalRedshirtRules): String =
        buildString {
            append("RS games played: ")
            append(player.rsGamesPlayedSeason)
            append(" (cap ")
            append(rules.maxGamesPlayed)
            append(")")
            if (player.injuryOccurredAtRsGameIndex > 0) {
                append(" · Schedule game ")
                append(player.injuryOccurredAtRsGameIndex)
                append(" (max ")
                append(rules.maxScheduleRsGameIndex)
                append(")")
            }
        }
}
