package dev.novux.dynasty.cbb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * API 34 emulator smoke — cold launch does not crash and shows shell entry ([MEGA-QA-119]).
 */
@RunWith(AndroidJUnit4::class)
class Api34AppLaunchSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun coldStart_showsHomeOrIntroContent() {
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = 20_000) {
            val hasTapToPlay = runCatching {
                composeRule.onNodeWithText("Tap to Play", ignoreCase = true).assertIsDisplayed()
            }.isSuccess
            val hasStudio = runCatching {
                composeRule.onNodeWithText("Novux Games", ignoreCase = true).assertIsDisplayed()
            }.isSuccess
            hasTapToPlay || hasStudio
        }
    }
}
