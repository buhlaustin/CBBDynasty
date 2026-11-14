package dev.novux.dynasty.cbb.core.pack

/** Display-only team fields from an identity pack — sim persists `teamId` only. */
data class TeamDisplayOverlay(
    val teamId: String,
    val conferenceId: String,
    val displayName: String,
    val shortName: String,
    val homeCity: String,
    val homeState: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val logoUri: String?,
    val postseasonEligible: Boolean,
    val programPrestige: Int,
)

data class ConferenceDisplayOverlay(
    val conferenceId: String,
    val displayName: String,
    val leagueGameCount: Int,
    val tournamentTemplateId: String?,
    val standingsTiebreakers: List<String> = emptyList(),
)
