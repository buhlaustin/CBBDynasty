package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.notification.NotificationFeedCommitRowPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitHubNotificationDeepLinkPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitInterestMeterUiPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitProspect
import dev.novux.dynasty.cbb.core.recruiting.RecruitSpendFeedbackPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubTab
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.signingday.IncomingSignee
import dev.novux.dynasty.cbb.core.signingday.SigningDayRosterEnrollPolicy
import dev.novux.dynasty.cbb.core.sim.PossessionShotClockViolationPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MegaBatch27GameplayPathTest {
    @Test
    fun recruitingAndSigningGameplayWired() {
        assertEquals("MEGA-RECR-085", RecruitInterestMeterUiPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-091", RecruitSpendFeedbackPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-092", RecruitHubNotificationDeepLinkPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-109", SigningDayRosterEnrollPolicy.MEGA_ID)
        assertEquals("MEGA-SIM-004", PossessionShotClockViolationPolicy.MEGA_ID)

        val prospect = RecruitProspect(
            prospectId = "p1",
            displayName = "Jordan Ellis",
            stars = 4,
            position = PlayerPosition.ShootingGuard,
            homeState = "OH",
            nilAskUsd = 120_000,
            scoutLetterOverall = "B+",
        )
        assertEquals("p1", RecruitHubNotificationDeepLinkPolicy.matchProspectIdByCommitName("Jordan Ellis", listOf(prospect)))
        assertEquals(
            "Jordan Ellis",
            NotificationFeedCommitRowPolicy.prospectNameFromFeedBody("Jordan Ellis signed with your program."),
        )
        assertEquals(
            RecruitingHubTab.Recommended,
            RecruitHubNotificationDeepLinkPolicy.resolveFocusTab("p1", setOf("p1"), emptySet()),
        )
        assertTrue(RecruitSpendFeedbackPolicy.shouldShowInterestGainConfirmation(3))

        val signee = IncomingSignee(
            prospectId = "p1",
            displayName = "Jordan Ellis",
            stars = 4,
            position = PlayerPosition.ShootingGuard,
            homeState = "OH",
            nilAskUsd = 100_000,
            nilSignedUsd = 90_000,
            nationalHsRank = 12,
            scoutLetterOverall = "A",
        )
        val enrolled = SigningDayRosterEnrollPolicy.enrollSignee("dyn", signee, depthOrder = 14, existingScholarshipCount = 10)
        assertNotNull(enrolled)
        assertEquals(signee.prospectId, enrolled!!.playerId)

        val root = repoRoot()
        assertTrue(
            root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/signingday/SigningDayRepository.kt")
                .readText()
                .contains("enrollIncomingSignees"),
        )
        assertTrue(
            root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/recruiting/RecruitingHubScreen.kt")
                .readText()
                .contains("recruiting_spend_interest_gain"),
        )
        assertTrue(
            root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dashboard/DashboardNotificationFeedPanel.kt")
                .readText()
                .contains("prospectNameFromFeedBody"),
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}