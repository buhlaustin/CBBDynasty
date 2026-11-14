package dev.novux.dynasty.cbb.ui.shell

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.BuildConfig
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.GameConstants
import dev.novux.dynasty.cbb.core.shell.SplashBootstrapPhase
import dev.novux.dynasty.cbb.core.shell.SplashPackErrorPolicy
import java.time.Year

/** Game splash — [planning/app-shell.md#splash-loading-screen]. */
@Composable
fun SplashScreen(
    bootstrapPhase: SplashBootstrapPhase,
    onTapToPlay: () -> Unit,
    onRetryLoad: () -> Unit,
) {
    val copyrightYear = Year.now().value
    val ready = bootstrapPhase == SplashBootstrapPhase.Ready
    val failed = bootstrapPhase == SplashBootstrapPhase.Error

    val ctaTransition = rememberInfiniteTransition(label = "tapToPlayPulse")
    val ctaScale by ctaTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tapToPlayScale",
    )
    val ctaAlpha by ctaTransition.animateFloat(
        initialValue = 0.78f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tapToPlayAlpha",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .sizeIn(maxHeight = 240.dp),
            )

            Spacer(modifier = Modifier.height(72.dp))

            Text(
                text = GameConstants.GAME_TITLE,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            when {
                failed && SplashPackErrorPolicy.shouldOfferRetry(true) -> {
                    Text(
                        text = stringResource(R.string.splash_load_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 28.dp),
                    )
                    Button(
                        onClick = onRetryLoad,
                        modifier = Modifier.padding(top = 16.dp),
                    ) {
                        Text(stringResource(R.string.splash_retry))
                    }
                }
                !ready -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 28.dp))
                }
                else -> {
                Text(
                    text = stringResource(R.string.tap_to_play),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 28.dp)
                        .graphicsLayer {
                            scaleX = ctaScale
                            scaleY = ctaScale
                            alpha = ctaAlpha
                        }
                        .clickable(onClick = onTapToPlay),
                )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.splash_copyright,
                    copyrightYear,
                    GameConstants.STUDIO_NAME,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.game_director_line),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = stringResource(R.string.splash_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
