package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.preseason.PreseasonRankingsReviewUxPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-072] */
class PreseasonRankingsReviewSheetPathTest {
    @Test
    fun preseasonRankingsReview_usesBottomSheetAndDismissOnContinue() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonScreen.kt",
        ).readText()
        val sheet = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonRankingsReviewBottomSheet.kt",
        ).readText()
        val hubRoute = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/hubs/RankingsHubRouteScreen.kt",
        ).readText()
        val deepLinks = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonDeepLinkNavigation.kt",
        ).readText()
        assertEquals("MEGA-FRAME-072", PreseasonRankingsReviewUxPolicy.MEGA_FRAME_ID)
        assertTrue(screen.contains("PreseasonRankingsReviewBottomSheet"))
        assertTrue(screen.contains("rankingsReviewSheetOpen"))
        assertTrue(sheet.contains("publishPreseasonRankings"))
        assertTrue(sheet.contains("onDismiss"))
        assertTrue(hubRoute.contains("onPublishPreseason = null"))
        assertTrue(deepLinks.contains("onOpenRankingsReviewSheet"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
