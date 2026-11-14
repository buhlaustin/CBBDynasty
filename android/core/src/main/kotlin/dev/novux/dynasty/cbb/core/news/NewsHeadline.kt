package dev.novux.dynasty.cbb.core.news

import kotlinx.serialization.Serializable

enum class NewsHeadlineKind {
    UserInjury,
    LeagueInjury,
    LeagueResult,
    RecruitingSpotlight,
    General,
    Awards,
    /** Weekly February NET bracketology ([MEGA-GDD-130]). */
    Bracketology,
    /** Weekly NET / poll movement summary ([MEGA-SHELL-137]). */
    PollNetMovement,
    PortalEntry,
    PortalCommit,
    PortalDecommit,
    PortalFlip,
}

@Serializable
data class NewsHeadline(
    val id: String,
    val seasonIndex: Int,
    val rsWeekIndex: Int? = null,
    val kind: NewsHeadlineKind,
    val title: String,
    val priority: Int = 0,
    val createdAtMs: Long = 0L,
)
