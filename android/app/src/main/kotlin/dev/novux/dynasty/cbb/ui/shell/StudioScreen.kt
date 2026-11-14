package dev.novux.dynasty.cbb.ui.shell

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import androidx.compose.ui.graphics.Color
import dev.novux.dynasty.cbb.core.GameConstants
import dev.novux.dynasty.cbb.core.shell.StudioBrandBackgroundPolicy
import kotlinx.coroutines.delay

/** Novux Games publisher beat — [planning/app-shell.md#studio-screen-novux-games]. */
@Composable
fun StudioScreen(
    onFinished: () -> Unit,
    autoAdvanceMs: Long = GameConstants.STUDIO_AUTO_ADVANCE_MS,
    minDisplayBeforeSkipMs: Long = GameConstants.STUDIO_MIN_SKIP_MS,
) {
    var skipEnabled by remember { mutableStateOf(false) }
    var fadeIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fadeIn = true
        delay(minDisplayBeforeSkipMs)
        skipEnabled = true
    }

    val logoAlpha by animateFloatAsState(
        targetValue = if (fadeIn) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "studioLogoFade",
    )

    var finished by remember { mutableStateOf(false) }
    val advance: () -> Unit = {
        if (!finished) {
            finished = true
            onFinished()
        }
    }

    LaunchedEffect(skipEnabled) {
        if (!skipEnabled) return@LaunchedEffect
        delay(autoAdvanceMs - minDisplayBeforeSkipMs)
        advance()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(StudioBrandBackgroundPolicy.backgroundArgb))
            .clickable(enabled = skipEnabled, onClick = advance)
            .alpha(logoAlpha),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 40.dp)
                .padding(bottom = 72.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.novux_studio_lockup),
                contentDescription = GameConstants.STUDIO_NAME,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .sizeIn(maxHeight = 168.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.studio_website),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = stringResource(R.string.studio_tap_to_continue),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
        )
    }
}
