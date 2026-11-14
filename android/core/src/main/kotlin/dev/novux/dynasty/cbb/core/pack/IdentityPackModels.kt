package dev.novux.dynasty.cbb.core.pack

import kotlinx.serialization.Serializable

/** Parsed identity pack (display + structural league graph). */
@Serializable
data class IdentityPack(
    val packVersion: Int,
    val membershipYear: String,
    val disclaimerAcceptedRequired: Boolean = true,
    val conferences: List<ConferencePack>,
    val teams: List<TeamPack>,
    val tournamentTemplates: List<TournamentTemplatePack> = emptyList(),
    val rulesetYear: Int,
    val packId: String? = null,
    val manifestSignerId: String? = null,
    val manifestSignature: String? = null,
)

@Serializable
data class ConferencePack(
    val id: String,
    val displayName: String,
    val leagueGameCount: Int,
    val standingsTiebreakers: List<String> = emptyList(),
    val tournamentTemplateId: String? = null,
    val ncaaAutoBidSlot: Int = 1,
)

@Serializable
data class TeamPack(
    val id: String,
    val conferenceId: String,
    val displayName: String,
    val shortName: String,
    val homeCity: String,
    val homeState: String,
    val postseasonEligible: Boolean = true,
    val reclassTransitionYear: Int? = null,
    val colors: TeamColorsPack,
    val arenaBaseName: String,
    val logoUri: String? = null,
    /** 0–99 when present; wizard uses [WizardProgramPrestige] when null. */
    val programPrestige: Int? = null,
)

@Serializable
data class TeamColorsPack(
    val primary: String,
    val secondary: String,
)

@Serializable
data class TournamentTemplatePack(
    val id: String,
    val bracketSize: Int,
    val seeds: Int,
    val byes: List<Int> = emptyList(),
    val reseedingAfterRound: Boolean = false,
    val sitePolicy: String,
    val qualificationPolicy: String,
    val rounds: List<TournamentRoundPack>,
)

@Serializable
data class TournamentRoundPack(
    val name: String,
    val games: Int,
)
