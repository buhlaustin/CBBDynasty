package dev.novux.dynasty.cbb.core.news

import dev.novux.dynasty.cbb.core.reports.ReportArticle
import dev.novux.dynasty.cbb.core.reports.ReportArticleType

/** Mini-article body for dashboard News tap → detail ([planning/news-hub.md#story-detail--articles]). */
object NewsHeadlineArticleBuilder {
    fun miniArticle(headline: NewsHeadline): ReportArticle {
        val paragraphs = paragraphsFor(headline)
        val pullQuote = when (headline.kind) {
            NewsHeadlineKind.RecruitingSpotlight -> "\"National rankings shift slowly — one commit can move the whole board.\""
            NewsHeadlineKind.Bracketology -> "\"Every NET swing in February reshapes the bubble conversation.\""
            NewsHeadlineKind.UserInjury -> "\"We will manage minutes and trust the depth behind him.\""
            else -> null
        }
        return ReportArticle(
            type = ReportArticleType.NewsStory,
            headline = headline.title,
            paragraphs = paragraphs,
            pullQuote = pullQuote,
        )
    }

  /** Headlines that should surface a hub CTA after opening detail. */
    fun prefersNewsHubTab(headline: NewsHeadline): Boolean = when (headline.kind) {
        NewsHeadlineKind.RecruitingSpotlight,
        NewsHeadlineKind.Awards,
        NewsHeadlineKind.Bracketology,
        -> true
        else -> false
    }

    private fun paragraphsFor(headline: NewsHeadline): List<String> {
        val title = headline.title
        val weekNote = headline.rsWeekIndex?.let { week ->
            "Week $week of the regular season."
        } ?: "League offices circulated the update league-wide."

        return when (headline.kind) {
            NewsHeadlineKind.UserInjury -> listOf(
                title,
                "Trainers evaluated the injury after practice and the program will update availability before the next game.",
                weekNote,
            )
            NewsHeadlineKind.LeagueInjury -> listOf(
                title,
                "Programs across the country are adjusting rotations as medical staffs clear players for contact.",
                "Expect updated depth charts before the upcoming slate.",
                weekNote,
            )
            NewsHeadlineKind.LeagueResult -> listOf(
                title,
                "The final margin rippled through conference standings and NET resumes will refresh overnight.",
                weekNote,
            )
            NewsHeadlineKind.RecruitingSpotlight -> listOf(
                title,
                "Blue-chip movement always draws attention on signing day calendars and portal windows alike.",
                "Full national lists and commit trackers live in the News hub Recruits tab.",
            )
            NewsHeadlineKind.Awards -> listOf(
                title,
                "The league announced postseason honors after reviewing game film and season-long metrics.",
                "Complete lists and past winners are archived in News hub Stories.",
            )
            NewsHeadlineKind.Bracketology -> listOf(
                title,
                "Analysts re-ranked at-large hopefuls using the latest NET sheet and recent resume games.",
                "Bracket projections update weekly through conference tournament week.",
            )
            NewsHeadlineKind.PollNetMovement -> listOf(
                title,
                "Synthetic media and coaches polls track the NET sheet as resumes update each week.",
                weekNote,
            )
            NewsHeadlineKind.PortalEntry,
            NewsHeadlineKind.PortalCommit,
            NewsHeadlineKind.PortalDecommit,
            NewsHeadlineKind.PortalFlip,
            -> listOf(
                title,
                "Transfer portal activity is tracked in News hub Portal during the offseason window.",
            )
            NewsHeadlineKind.General -> listOf(
                title,
                weekNote,
                "More context and related headlines appear in News hub when you want the full chronology.",
            )
        }
    }
}
