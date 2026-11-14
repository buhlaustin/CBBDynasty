package dev.novux.dynasty.cbb.ui.shell

import android.content.Context
import dev.novux.dynasty.cbb.core.dashboard.InSeasonWeekGames
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.core.schedule.PreseasonSchedulePlan
import dev.novux.dynasty.cbb.core.school.SchoolHubOverview
import dev.novux.dynasty.cbb.requireDynastyApplication
import dev.novux.dynasty.cbb.ui.preview.GamePreviewFromCalendar

/** Opens game preview for the active week's first calendar game ([MEGA-SHELL-055]). */
object ShellWeekGamePreviewLauncher {
    suspend fun launchCurrentWeek(
        context: Context,
        loaded: LoadedDynasty,
        userSchool: SchoolHubOverview,
        schedulePlan: PreseasonSchedulePlan?,
        exhibitionCount: Int,
        onOpenGamePreview: () -> Unit,
    ) {
        val game = InSeasonWeekGames.forPosition(
            loaded.weekCheckpoint.activePosition,
            schedulePlan,
            exhibitionCount,
        ).firstOrNull() ?: return
        val app = context.requireDynastyApplication()
        val oppOverview = if (!game.opponentTeamId.startsWith("event-")) {
            app.schoolHubRepository.loadOpponentOverview(
                loaded.dynastyId,
                game.opponentTeamId,
                loaded.metadata.seasonIndex,
            )
        } else {
            null
        }
        GamePreviewFromCalendar.launchFromStub(
            context = context,
            dynastyId = loaded.dynastyId,
            seasonIndex = loaded.metadata.seasonIndex,
            userSchool = userSchool,
            game = game,
            oppOverview = oppOverview,
        )
        onOpenGamePreview()
    }
}