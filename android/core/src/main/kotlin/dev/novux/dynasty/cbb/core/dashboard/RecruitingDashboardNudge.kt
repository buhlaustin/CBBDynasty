package dev.novux.dynasty.cbb.core.dashboard

/**
 * In-season recruiting optional dashboard nudges ([planning/recruiting-engine.md#in-season-dashboard-tasks]).
 */
enum class RecruitingDashboardNudgeKind {
    WatchlistOffer,
    WatchlistReEngage,
    Top60Movement,
    RivalCommit,
}

data class RecruitingDashboardNudge(
    val kind: RecruitingDashboardNudgeKind,
    val prospectName: String,
    val nationalRank: Int? = null,
    val rivalSchoolName: String? = null,
)

fun RecruitingDashboardNudge.toDashboardTask(): DashboardTask {
    val id = when (kind) {
        RecruitingDashboardNudgeKind.WatchlistOffer -> DashboardTaskId.RecruitingWatchlistOffer
        RecruitingDashboardNudgeKind.WatchlistReEngage -> DashboardTaskId.RecruitingWatchlistReEngage
        RecruitingDashboardNudgeKind.Top60Movement -> DashboardTaskId.RecruitingTop60Movement
        RecruitingDashboardNudgeKind.RivalCommit -> DashboardTaskId.RecruitingRivalCommit
    }
    val deepLink = DashboardDeepLink.RecruitingRecommended
    val formatArgs = buildList {
        nationalRank?.let { add(it) }
    }
    val stringArgs = buildList {
        add(prospectName)
        rivalSchoolName?.let { add(it) }
    }
    return DashboardTask(
        id = id,
        kind = DashboardTaskKind.Optional,
        deepLink = deepLink,
        formatArgs = formatArgs,
        stringFormatArgs = stringArgs,
        dismissScopeKey = "${kind.name}:$prospectName",
    )
}
