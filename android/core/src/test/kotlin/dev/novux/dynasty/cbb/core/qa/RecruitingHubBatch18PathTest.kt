package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.nil.RecruitNilPartialOfferPolicy
import dev.novux.dynasty.cbb.core.pack.NewGameStarterPrestigeFilterPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitNilInterestBadgeRefreshPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitScholarshipPromiseDeltaPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingClosedCycleWeekGrantPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCycleLedger
import dev.novux.dynasty.cbb.core.signingday.SigningDayNilSnapshotPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitingHubBatch18PathTest {
    @Test
    fun batch18PoliciesWiredInUi() {
        assertEquals("MEGA-RECR-075", NewGameStarterPrestigeFilterPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-090", RecruitScholarshipPromiseDeltaPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-093", RecruitNilPartialOfferPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-097", RecruitNilInterestBadgeRefreshPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-098", SigningDayNilSnapshotPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-104", RecruitingClosedCycleWeekGrantPolicy.MEGA_ID)
        assertTrue(RecruitScholarshipPromiseDeltaPolicy.promiseDelta(null) == 0)
        val snap = SigningDayNilSnapshotPolicy.applyNilOffer(
            dev.novux.dynasty.cbb.core.signingday.IncomingSignee(
                prospectId = "p",
                displayName = "Test",
                stars = 3,
                position = dev.novux.dynasty.cbb.core.roster.PlayerPosition.PointGuard,
                homeState = "OH",
                nilAskUsd = 10_000,
                nationalHsRank = 100,
                scoutLetterOverall = "B",
            ),
            nilOfferUsd = 5_000,
        )
        assertEquals(5_000, snap.nilSignedUsd)
        assertTrue(
            RecruitingClosedCycleWeekGrantPolicy.skipsWeeklyGrant(
                RecruitingCycleLedger(
                    cycleSeasonIndex = 1,
                    programPrestige = 40,
                    weeklyPoolBase = 10,
                    pointsAvailable = 0,
                    cycleClosed = true,
                ),
            ),
        )
        val root = repoRoot()
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/newgame/SchoolStepContent.kt",
            ).readText().contains("NewGameStarterPrestigeFilterPolicy"),
        )
        val hub = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/recruiting/RecruitingHubScreen.kt",
        ).readText()
        assertTrue(hub.contains("offerPromisesByProspect"))
        assertTrue(hub.contains("RecruitNilPartialOfferControls"))
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/signingday/SigningDayRepository.kt",
            ).readText().contains("SigningDayNilSnapshotPolicy"),
        )
        assertTrue(
            RecruitNilInterestBadgeRefreshPolicy.badgeScoreForRow(30.0, liveInterest = 72, nilOfferUsd = 1_000) >= 72.0,
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}