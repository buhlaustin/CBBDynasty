package dev.novux.dynasty.cbb.ui.portrait

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.core.portrait.PresetPortraitRenderer

@Composable
fun PresetPortrait(
    presetId: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    contentDescription: String? = null,
) {
    val spec = remember(presetId) { PresetPortraitRenderer.specFor(presetId) }
    val skin = Color(spec.skinToneArgb)
    val hair = Color(spec.hairColorArgb)
    val descModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    Canvas(
        modifier = modifier
            .then(descModifier)
            .size(size)
            .clip(CircleShape),
    ) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val faceR = w * 0.38f
        drawCircle(color = skin, radius = faceR, center = Offset(cx, h * 0.55f))
        when (spec.hairStyleId) {
            "hair_bald" -> Unit
            "hair_curly" -> {
                drawCircle(color = hair, radius = faceR * 1.05f, center = Offset(cx, h * 0.42f))
                drawCircle(color = hair.copy(alpha = 0.85f), radius = faceR * 0.35f, center = Offset(cx - faceR * 0.5f, h * 0.35f))
                drawCircle(color = hair.copy(alpha = 0.85f), radius = faceR * 0.35f, center = Offset(cx + faceR * 0.5f, h * 0.35f))
            }
            "hair_fade" -> {
                drawArc(
                    color = hair,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = true,
                    topLeft = Offset(cx - faceR * 1.1f, h * 0.08f),
                    size = Size(faceR * 2.2f, faceR * 1.4f),
                )
                drawRect(
                    color = hair,
                    topLeft = Offset(cx - faceR * 1.05f, h * 0.22f),
                    size = Size(faceR * 2.1f, faceR * 0.25f),
                )
            }
            else -> {
                drawArc(
                    color = hair,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(cx - faceR * 1.15f, h * 0.05f),
                    size = Size(faceR * 2.3f, faceR * 1.5f),
                )
            }
        }
        drawCircle(
            color = Color.Black.copy(alpha = 0.65f),
            radius = w * 0.045f,
            center = Offset(cx - faceR * 0.35f, h * 0.52f),
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.65f),
            radius = w * 0.045f,
            center = Offset(cx + faceR * 0.35f, h * 0.52f),
        )
    }
}
