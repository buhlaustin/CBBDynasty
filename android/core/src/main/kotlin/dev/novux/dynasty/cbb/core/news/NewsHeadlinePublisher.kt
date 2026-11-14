package dev.novux.dynasty.cbb.core.news

import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.injuries.InjurySeverity
import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.awards.AwardHonorLine
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import java.util.UUID

/** Shallow headline copy ([planning/news-hub.md], [planning/injuries.md#news--league]). */
object NewsHeadlinePublisher {
    const val SHALLOW_FEED_CAP: Int = 10

    fun userInjury(
        seasonIndex: Int,
        rsWeekIndex: Int,
        playerName: String,
        event: GameSimInjuryEvent,
    ): NewsHeadline = NewsHeadline(
        id = UUID.randomUUID().toString(),
        seasonIndex = seasonIndex,
        rsWeekIndex = rsWeekIndex,
        kind = NewsHeadlineKind.UserInjury,
        title = "Your program: $playerName — ${event.label} (${event.weeksRemaining} weeks)",
        priority = 100,
        createdAtMs = System.currentTimeMillis(),
    )

    fun userInjuryFromRoster(
        seasonIndex: Int,
        rsWeekIndex: Int,
        player: RosterPlayer,
    ): NewsHeadline? {
        if (!player.injuryActive) return null
        val label = player.injuryLabel ?: "Injury"
        return NewsHeadline(
            id = "user-inj-${player.playerId}",
            seasonIndex = seasonIndex,
            rsWeekIndex = rsWeekIndex,
            kind = NewsHeadlineKind.UserInjury,
            title = "Injury report: ${player.displayName.ifBlank { player.playerId }} — $label (${player.injuryWeeksRemaining}w)",
            priority = 95,
            createdAtMs = System.currentTimeMillis(),
        )
    }

    fun leagueInjuryWire(seasonIndex: Int, rsWeekIndex: Int, programsAffected: Int): NewsHeadline? {
        if (programsAffected <= 0) return null
        val tier = when {
            programsAffected >= 12 -> InjurySeverity.Major
            programsAffected >= 5 -> InjurySeverity.Moderate
            else -> InjurySeverity.Minor
        }
        val priority = when (tier) {
            InjurySeverity.Major -> 80
            InjurySeverity.Moderate -> 55
            InjurySeverity.Minor -> 35
        }
        val scope = when (tier) {
            InjurySeverity.Major -> "Several high-profile programs"
            InjurySeverity.Moderate -> "League programs"
            InjurySeverity.Minor -> "Programs around the country"
        }
        return NewsHeadline(
            id = "league-inj-$seasonIndex-$rsWeekIndex",
            seasonIndex = seasonIndex,
            rsWeekIndex = rsWeekIndex,
            kind = NewsHeadlineKind.LeagueInjury,
            title = "$scope report key injuries (Week $rsWeekIndex)",
            priority = priority,
            createdAtMs = System.currentTimeMillis(),
        )
    }

    fun leagueGameResult(seasonIndex: Int, result: LeagueGameResult): NewsHeadline {
        val margin = kotlin.math.abs(result.homeScore - result.awayScore)
        val priority = 20 + margin.coerceAtMost(30)
        return NewsHeadline(
            id = "lg-${result.homeTeamId}-${result.awayTeamId}-w${result.rsWeekIndex}",
            seasonIndex = seasonIndex,
            rsWeekIndex = result.rsWeekIndex,
            kind = NewsHeadlineKind.LeagueResult,
            title = "${result.awayTeamId} @ ${result.homeTeamId}: ${result.homeScore}-${result.awayScore}",
            priority = priority,
            createdAtMs = System.currentTimeMillis(),
        )
    }

    fun yearlyAward(seasonIndex: Int, honor: AwardHonorLine, priorityBoost: Int = 0): NewsHeadline = NewsHeadline(
        id = "award-$seasonIndex-${honor.awardLabel.filter { it.isLetterOrDigit() }}",
        seasonIndex = seasonIndex,
        kind = NewsHeadlineKind.Awards,
        title = "${honor.awardLabel}: ${honor.winnerName} (${honor.schoolName})",
        priority = 70 + priorityBoost,
        createdAtMs = System.currentTimeMillis(),
    )

    fun awardsWrap(seasonIndex: Int, honors: List<AwardHonorLine>): List<NewsHeadline> =
        honors.mapIndexed { index, line -> yearlyAward(seasonIndex, line, priorityBoost = index) }

    fun recruitingSpotlight(seasonIndex: Int, headline: String): NewsHeadline = NewsHeadline(
        id = "top60-$seasonIndex",
        seasonIndex = seasonIndex,
        kind = NewsHeadlineKind.RecruitingSpotlight,
        title = headline,
        priority = 40,
        createdAtMs = System.currentTimeMillis(),
    )

    fun bracketology(headline: NewsHeadline): NewsHeadline =
        headline.copy(kind = NewsHeadlineKind.Bracketology)

    fun portalEntry(
        seasonIndex: Int,
        playerName: String,
        priorSchool: String,
        createdAtMs: Long = System.currentTimeMillis(),
    ): NewsHeadline = NewsHeadline(
        id = UUID.randomUUID().toString(),
        seasonIndex = seasonIndex,
        kind = NewsHeadlineKind.PortalEntry,
        title = "$playerName enters transfer portal ($priorSchool)",
        priority = 55,
        createdAtMs = createdAtMs,
    )

    fun portalCommit(
        seasonIndex: Int,
        playerName: String,
        schoolName: String,
        createdAtMs: Long = System.currentTimeMillis(),
    ): NewsHeadline = NewsHeadline(
        id = UUID.randomUUID().toString(),
        seasonIndex = seasonIndex,
        kind = NewsHeadlineKind.PortalCommit,
        title = "$playerName commits to $schoolName",
        priority = 60,
        createdAtMs = createdAtMs,
    )
}
