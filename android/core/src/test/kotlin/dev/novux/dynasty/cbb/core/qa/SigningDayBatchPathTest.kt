package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.dashboard.SigningDayRequiredTasksPolicy
import dev.novux.dynasty.cbb.core.news.RecruitNewsVsTeamRankingsPolicy
import dev.novux.dynasty.cbb.core.preseason.PreseasonRankingsReviewNoShortcutPolicy
import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId
import dev.novux.dynasty.cbb.core.signingday.SigningDayCycleClosePolicy
import dev.novux.dynasty.cbb.core.signingday.SigningDayPositionEditorPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SigningDayBatchPathTest {
    @Test
    fun signingDayAndRankingsPoliciesWired() {
        assertEquals("MEGA-RECR-046", SigningDayRequiredTasksPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-050", SigningDayPositionEditorPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-051", SigningDayCycleClosePolicy.MEGA_ID)
        assertEquals("MEGA-RECR-054", RecruitNewsVsTeamRankingsPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-056", PreseasonRankingsReviewNoShortcutPolicy.MEGA_ID)
        assertTrue(
            PreseasonRankingsReviewNoShortcutPolicy.blocksGenericCompleteButton(PreseasonTaskId.RankingsReview),
        )
        val root = repoRoot()
        val signing = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/signingday/SigningDayScreen.kt",
        ).readText()
        assertTrue(signing.contains("ensurePool"))
        assertTrue(signing.contains("closeCycleAfterHardSign"))
        assertTrue(signing.contains("signing_day_required_dashboard_hint"))
        assertTrue(signing.contains("signing_day_cycle_close_hint"))
        val news = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/news/NewsHubScreen.kt",
        ).readText()
        assertTrue(news.contains("news_hub_recruits_scope"))
        val preseason = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonFeatureTaskPanels.kt",
        ).readText()
        assertTrue(preseason.contains("preseason_rankings_no_shortcut_hint"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}