package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.league.LeagueWeekBatchPerformanceBudget
import dev.novux.dynasty.cbb.core.pack.IdentityPackCodec
import dev.novux.dynasty.cbb.core.pack.IdentityPackIndex
import dev.novux.dynasty.cbb.core.pack.PackLoadResult
import dev.novux.dynasty.cbb.core.rankings.RankingsHubSnapshotFactory
import dev.novux.dynasty.cbb.core.rankings.RankingsHubUiPolicy
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-055] */
class RankingsHub365PathTest {
    @Test
    fun publishedSnapshot_listsFullLeagueOnEmbeddedPack() {
        val raw = repoRoot().resolve("android/app/src/main/assets/pack/embedded-default.json").readText()
        val pack = (IdentityPackCodec.parseAndValidate(raw) as PackLoadResult.Success).pack
        val index = IdentityPackIndex.from(pack)
        val snapshot = RankingsHubSnapshotFactory.build(index, standings = null, rankingsPublished = true)
        assertEquals(LeagueWeekBatchPerformanceBudget.FULL_LEAGUE_TEAM_COUNT, snapshot.net.size)
        assertEquals(snapshot.net.size, snapshot.media.size)
        assertEquals(snapshot.net.size, snapshot.coaches.size)
    }

    @Test
    fun rankingsHubScreen_usesPlaceholderAndTabGate() {
        val screen = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/rankings/RankingsHubScreen.kt",
        ).readText()
        val repo = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/rankings/RankingsHubRepository.kt",
        ).readText()
        assertTrue(screen.contains("rankings_hub_placeholder"))
        assertTrue(screen.contains("snapshot.published"))
        assertTrue(screen.contains("PrimaryTabRow"))
        assertTrue(screen.contains("UNPUBLISHED_PREVIEW_CAP"))
        assertTrue(repo.contains("RankingsHubPublishPolicy"))
    }

    private fun repoRoot(): Path {
        var dir = Path.of("").toAbsolutePath()
        while (!dir.resolve("android/settings.gradle.kts").toFile().exists()) {
            dir = dir.parent ?: error("repo root not found")
        }
        return dir
    }
}
