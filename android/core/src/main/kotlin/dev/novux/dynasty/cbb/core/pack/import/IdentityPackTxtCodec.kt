package dev.novux.dynasty.cbb.core.pack.import

import dev.novux.dynasty.cbb.core.pack.ConferencePack
import dev.novux.dynasty.cbb.core.pack.IdentityPack
import dev.novux.dynasty.cbb.core.pack.PackConstants
import dev.novux.dynasty.cbb.core.pack.TeamColorsPack
import dev.novux.dynasty.cbb.core.pack.TeamPack
import dev.novux.dynasty.cbb.core.pack.TournamentRoundPack
import dev.novux.dynasty.cbb.core.pack.TournamentTemplatePack

/**
 * Line-oriented TXT import — see `assets/pack/identity-import-template.txt`.
 */
object IdentityPackTxtCodec {
  enum class ImportMode { Full, Overlay }

  fun parseOrThrow(raw: String): Pair<IdentityPack, ImportMode> = parseInternal(raw)

  private fun parseInternal(raw: String): Pair<IdentityPack, ImportMode> {
    val meta = mutableMapOf<String, String>()
    var mode = ImportMode.Full
    val conferences = mutableListOf<ConferencePack>()
    val teams = mutableListOf<TeamPack>()
    val templates = mutableListOf<TournamentTemplatePack>()

    var sectionType: String? = null
    var sectionId: String? = null
    val sectionFields = mutableMapOf<String, String>()

    fun flushSection() {
      val type = sectionType ?: return
      val id = sectionId?.takeIf { it.isNotBlank() }
          ?: throw TxtParseException("Section [$type] missing id")
      when (type.lowercase()) {
        "conference" -> conferences += parseConference(id, sectionFields)
        "team" -> teams += parseTeam(id, sectionFields)
        "tournament" -> templates += parseTournament(id, sectionFields)
        else -> throw TxtParseException("Unknown section type: $type")
      }
      sectionFields.clear()
    }

    raw.lineSequence().forEach { line ->
      val trimmed = line.trim()
      if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
      if (trimmed.startsWith("@")) {
        flushSection()
        sectionType = null
        sectionId = null
        val body = trimmed.removePrefix("@")
        val eq = body.indexOf('=')
        if (eq <= 0) throw TxtParseException("Bad meta line: $trimmed")
        meta[body.substring(0, eq).trim()] = body.substring(eq + 1).trim()
        return@forEach
      }
      val header = Regex("""^\[(.+?)\s+(.+?)\]$""").matchEntire(trimmed)
      if (header != null) {
        flushSection()
        sectionType = header.groupValues[1]
        sectionId = header.groupValues[2]
        return@forEach
      }
      if (sectionType == null) {
        val kv = splitKeyValue(trimmed) ?: throw TxtParseException("Expected meta or section: $trimmed")
        when (kv.first.lowercase()) {
          "mode" -> mode = if (kv.second.equals("overlay", true)) ImportMode.Overlay else ImportMode.Full
          else -> meta[kv.first] = kv.second
        }
        return@forEach
      }
      val kv = splitKeyValue(trimmed) ?: throw TxtParseException("Bad field line: $trimmed")
      sectionFields[kv.first] = kv.second
    }
    flushSection()

    val packVersion = meta["packVersion"]?.toIntOrNull() ?: PackConstants.SUPPORTED_PACK_VERSION
    val membershipYear = meta["membershipYear"] ?: throw TxtParseException("membershipYear required")
    val rulesetYear = meta["rulesetYear"]?.toIntOrNull() ?: throw TxtParseException("rulesetYear required")
    val disclaimer = meta["disclaimerAcceptedRequired"]?.toBooleanStrictOrNull() ?: true
    val packId = meta["packId"]

    val pack = IdentityPack(
        packVersion = packVersion,
        membershipYear = membershipYear,
        disclaimerAcceptedRequired = disclaimer,
        conferences = conferences,
        teams = teams,
        tournamentTemplates = templates,
        rulesetYear = rulesetYear,
        packId = packId,
        manifestSignerId = meta["manifestSignerId"],
        manifestSignature = meta["manifestSignature"],
    )
    return pack to mode
  }

  private fun splitKeyValue(line: String): Pair<String, String>? {
    val eq = line.indexOf('=')
    if (eq <= 0) return null
    return line.substring(0, eq).trim() to line.substring(eq + 1).trim()
  }

  private fun parseConference(id: String, fields: Map<String, String>): ConferencePack {
    val displayName = fields["displayName"] ?: throw TxtParseException("conference $id: displayName required")
    val leagueGameCount = fields["leagueGameCount"]?.toIntOrNull()
        ?: throw TxtParseException("conference $id: leagueGameCount required")
    val tiebreakers = fields["standingsTiebreakers"]?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }
        ?: emptyList()
    return ConferencePack(
        id = id,
        displayName = displayName,
        leagueGameCount = leagueGameCount,
        standingsTiebreakers = tiebreakers,
        tournamentTemplateId = fields["tournamentTemplateId"]?.takeIf { it.isNotBlank() },
        ncaaAutoBidSlot = fields["ncaaAutoBidSlot"]?.toIntOrNull() ?: 1,
    )
  }

  private fun parseTeam(id: String, fields: Map<String, String>): TeamPack {
    val conferenceId = fields["conferenceId"] ?: throw TxtParseException("team $id: conferenceId required")
    val displayName = fields["displayName"] ?: throw TxtParseException("team $id: displayName required")
    val shortName = fields["shortName"] ?: throw TxtParseException("team $id: shortName required")
    val primary = fields["colors.primary"] ?: fields["primaryColor"]
        ?: throw TxtParseException("team $id: colors.primary required")
    val secondary = fields["colors.secondary"] ?: fields["secondaryColor"]
        ?: throw TxtParseException("team $id: colors.secondary required")
    return TeamPack(
        id = id,
        conferenceId = conferenceId,
        displayName = displayName,
        shortName = shortName,
        homeCity = fields["homeCity"] ?: "",
        homeState = fields["homeState"] ?: "",
        postseasonEligible = fields["postseasonEligible"]?.toBooleanStrictOrNull() ?: true,
        reclassTransitionYear = fields["reclassTransitionYear"]?.toIntOrNull(),
        colors = TeamColorsPack(primary, secondary),
        arenaBaseName = fields["arenaBaseName"] ?: "Arena",
        logoUri = fields["logoUri"]?.takeIf { it.isNotBlank() },
    )
  }

  private fun parseTournament(id: String, fields: Map<String, String>): TournamentTemplatePack {
    val bracketSize = fields["bracketSize"]?.toIntOrNull()
        ?: throw TxtParseException("tournament $id: bracketSize required")
    val seeds = fields["seeds"]?.toIntOrNull() ?: bracketSize
    val byes = fields["byes"]?.split(',')?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
    val rounds = parseRounds(fields["round"] ?: fields["rounds"])
    return TournamentTemplatePack(
        id = id,
        bracketSize = bracketSize,
        seeds = seeds,
        byes = byes,
        reseedingAfterRound = fields["reseedingAfterRound"]?.toBooleanStrictOrNull() ?: false,
        sitePolicy = fields["sitePolicy"] ?: throw TxtParseException("tournament $id: sitePolicy required"),
        qualificationPolicy = fields["qualificationPolicy"]
            ?: throw TxtParseException("tournament $id: qualificationPolicy required"),
        rounds = rounds,
    )
  }

  private fun parseRounds(spec: String?): List<TournamentRoundPack> {
    if (spec.isNullOrBlank()) throw TxtParseException("tournament rounds required")
    return spec.split(',').map { token ->
      val parts = token.trim().split(':')
      if (parts.size != 2) throw TxtParseException("round must be Name:games")
      val games = parts[1].toIntOrNull() ?: throw TxtParseException("round games invalid")
      TournamentRoundPack(parts[0].trim(), games)
    }
  }
}

private class TxtParseException(message: String) : IllegalArgumentException(message)
