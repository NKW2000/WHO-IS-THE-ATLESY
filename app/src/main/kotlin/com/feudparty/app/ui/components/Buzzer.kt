package com.feudparty.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudColors

/**
 * زر الجواب — دائرة كرتونية بحد أسود سميك وظل صلب تحتها. بتطفو (bob) لما
 * تكون مفتوحة، وبتنزل على ظلها لحظة الضغط، وبيهتزّ الجهاز.
 */
@Composable
fun BuzzerButton(
    label: String,
    subLabel: String?,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp
) {
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val transition = rememberInfiniteTransition(label = "buzzer")
    val float by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (enabled) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(1_800), RepeatMode.Reverse),
        label = "bob"
    )
    val drop by animateFloatAsState(
        targetValue = if (pressed && enabled) 1f else 0f,
        animationSpec = tween(110),
        label = "press"
    )

    LaunchedEffect(pressed) {
        if (pressed && enabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val shadowDepth = 12.dp
    val lift = (float * 6).dp
    Box(modifier = modifier.size(size + shadowDepth), contentAlignment = Alignment.Center) {
        // الظل الصلب تحت الزر.
        Box(
            modifier = Modifier
                .size(size)
                .offset(y = shadowDepth - lift)
                .background(FeudColors.ink, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(size)
                .offset(y = -lift + (drop * shadowDepth.value).dp)
                .scale(if (pressed && enabled) 0.97f else 1f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.lighten(), accent),
                        center = Offset(0.32f, 0.22f)
                    ),
                    CircleShape
                )
                .border(7.dp, FeudColors.ink, CircleShape)
                .clickable(
                    enabled = enabled,
                    interactionSource = interaction,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 18.dp)
            ) {
                Text(
                    label,
                    color = if (accent.isLight()) FeudColors.ink else Color.White,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center
                )
                if (subLabel != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        subLabel,
                        color = if (accent.isLight()) {
                            FeudColors.ink.copy(alpha = 0.7f)
                        } else {
                            Color.White.copy(alpha = 0.85f)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun Color.lighten(amount: Float = 0.25f): Color = Color(
    red = red + (1f - red) * amount,
    green = green + (1f - green) * amount,
    blue = blue + (1f - blue) * amount,
    alpha = alpha
)

private fun Color.isLight(): Boolean = (red * 0.299f + green * 0.587f + blue * 0.114f) > 0.6f
