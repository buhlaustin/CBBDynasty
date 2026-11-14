package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.dashboard.InSeasonRecruitingOptionalDismissPolicy
import dev.novux.dynasty.cbb.core.dashboard.RecruitingDashboardNudge
import dev.novux.dynasty.cbb.core.dashboard.RecruitingDashboardNudgeKind
import dev.novux.dynasty.cbb.core.dashboard.toDashboardTask
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InSeasonRecruitingOptionalDismissPathTest {
    @Test
    fun recruitingNudgesUsePerProspectDismissScope() {
        assertEquals("MEGA-RECR-047", InSeasonRecruitingOptionalDismissPolicy.MEGA_ID)
        val position = CalendarPosition(seasonIndex = 1, phase = CalendarPhase.RegularSeason, weekIndex = 5)
        val a = RecruitingDashboardNudge(RecruitingDashboardNudgeKind.WatchlistOffer, "Prospect A").toDashboardTask()
        val b = RecruitingDashboardNudge(RecruitingDashboardNudgeKind.WatchlistOffer, "Prospect B").toDashboardTask()
        assertNotEquals(
            InSeasonRecruitingOptionalDismissPolicy.dismissKey(position, a),
            InSeasonRecruitingOptionalDismissPolicy.dismissKey(position, b),
        )
        val shell = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt",
        ).readText()
        assertTrue(shell.contains("DashboardTaskDismissal.key"))
        assertTrue(
            repoRoot().resolve(
                "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/dashboard/RecruitingDashboardNudge.kt",
            ).readText().contains("dismissScopeKey"),
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}