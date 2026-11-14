package dev.novux.dynasty.cbb.core.pack

import dev.novux.dynasty.cbb.core.save.DynastySchema

/**
 * Read-only lookup tables for pack overlay — [planning/game-design.md#save-partitioning].
 * Rebuilt on pack swap; never mutates dynasty history rows.
 */
class IdentityPackIndex private constructor(
    val source: IdentityPack,
    private val teamsById: Map<String, TeamPack>,
    private val conferencesById: Map<String, ConferencePack>,
) {
    val packId: String
        get() = source.packId ?: DynastySchema.DEFAULT_PACK_ID

    val membershipYear: String
        get() = source.membershipYear

    fun team(teamId: String): TeamDisplayOverlay? {
        val team = teamsById[teamId] ?: return null
        return TeamDisplayOverlay(
            teamId = team.id,
            conferenceId = team.conferenceId,
            displayName = team.displayName,
            shortName = team.shortName,
            homeCity = team.homeCity,
            homeState = team.homeState,
            primaryColorHex = team.colors.primary,
            secondaryColorHex = team.colors.secondary,
            logoUri = team.logoUri,
            postseasonEligible = team.postseasonEligible,
            programPrestige = WizardProgramPrestige.effective(team),
        )
    }

    fun conference(confId: String): ConferenceDisplayOverlay? {
        val conf = conferencesById[confId] ?: return null
        return ConferenceDisplayOverlay(
            conferenceId = conf.id,
            displayName = conf.displayName,
            leagueGameCount = conf.leagueGameCount,
            tournamentTemplateId = conf.tournamentTemplateId,
            standingsTiebreakers = conf.standingsTiebreakers,
        )
    }

    fun allTeamIds(): Set<String> = teamsById.keys

    fun allTeamDisplays(): List<TeamDisplayOverlay> =
        teamsById.values
            .mapNotNull { team(it.id) }
            .sortedBy { it.displayName.lowercase() }

    fun allConferenceDisplays(): List<ConferenceDisplayOverlay> =
        conferencesById.keys
            .mapNotNull { conference(it) }
            .sortedBy { it.displayName.lowercase() }

    fun tournamentTemplate(templateId: String): TournamentTemplatePack? =
        source.tournamentTemplates.firstOrNull { it.id == templateId }

    fun teamsInConference(conferenceId: String): List<TeamDisplayOverlay> =
        allTeamDisplays().filter { it.conferenceId == conferenceId }

    companion object {
        fun from(pack: IdentityPack): IdentityPackIndex {
            val teams = pack.teams.associateBy { it.id }
            val confs = pack.conferences.associateBy { it.id }
            return IdentityPackIndex(
                source = pack,
                teamsById = teams,
                conferencesById = confs,
            )
        }
    }
}
