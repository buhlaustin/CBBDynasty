package dev.novux.dynasty.cbb.ui.navigation

/**
 * Pre-dynasty shell — [planning/app-shell.md], [planning/game-start.md].
 */
sealed class AppRoute(val route: String) {
    data object Studio : AppRoute("studio")

    data object Splash : AppRoute("splash")

    data object Home : AppRoute("home")

    data object Settings : AppRoute("settings")

    data object DynastyAutomation : AppRoute("dynasty_automation")

    data object Credits : AppRoute("credits")

    data object PackImport : AppRoute("pack_import")

    data object Load : AppRoute("load")

    data object NewGame : AppRoute("new_game")

    data object SeasonReview : AppRoute("season_review")

    /** Unified dynasty career shell — phase swaps body only ([MEGA-FRAME-005]). */
    data object DynastyCareer : AppRoute("dynasty_career")

    @Deprecated("Use DynastyCareer", ReplaceWith("DynastyCareer"))
    data object Offseason : AppRoute("offseason")

    @Deprecated("Use DynastyCareer", ReplaceWith("DynastyCareer"))
    data object Preseason : AppRoute("preseason")

    /** @deprecated Use [DynastyCareer] — [planning/game-frame.md]. */
  @Deprecated("Use DynastyCareer", ReplaceWith("DynastyCareer"))
    data object DynastyShell : AppRoute("dynasty_shell")

    data object Strategy : AppRoute("strategy")

    data object GameWatch : AppRoute("game_watch")

    data object GamePreview : AppRoute("game_preview")

    data object Discipline : AppRoute("discipline")

    data object CoachingCarousel : AppRoute("coaching_carousel")

    data object Recruiting : AppRoute("recruiting")

    data object SigningDay : AppRoute("signing_day")

    data object NewsHub : AppRoute("news_hub")

    data object SchoolHub : AppRoute("school_hub")

    data object StatisticsHub : AppRoute("statistics_hub")

    data object RankingsHub : AppRoute("rankings_hub")

    data object HistoryHub : AppRoute("history_hub")

    data object ConferenceTournamentHub : AppRoute("conference_tournament_hub")

    data object MarchMayhem : AppRoute("march_mayhem")

    data object AwardsHub : AppRoute("awards_hub")

    data object JobCenter : AppRoute("job_center")
}
