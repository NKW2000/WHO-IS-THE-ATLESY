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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudColors

/**
 * زر البزّ — أكبر عنصر بشاشة الفريق. بينبض لما يكون مفتوح، وبينطفي لما
 * يقفل، وبيهتزّ الجهاز لحظة الضغط.
 */
@Composable
fun BuzzerButton(
    label: String,
    subLabel: String?,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val pulse = if (enabled) {
        val transition = rememberInfiniteTransition(label = "buzzPulse")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(tween(680), RepeatMode.Reverse),
            label = "pulseScale"
        ).value
    } else {
        1f
    }
    val press by animateFloatAsState(if (pressed) 0.94f else 1f, tween(90), label = "press")
    val scale = pulse * press

    val face = if (enabled) {
        Brush.radialGradient(listOf(accent, accent.copy(alpha = 0.55f), FeudColors.panelDark))
    } else {
        Brush.radialGradient(listOf(FeudColors.panel, FeudColors.panelDark))
    }

    Box(
        modifier = modifier
            .size(250.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(face, CircleShape)
            .border(
                6.dp,
                if (enabled) FeudColors.gold else FeudColors.goldDim.copy(alpha = 0.4f),
                CircleShape
            )
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                color = if (enabled) FeudColors.deepNavy else FeudColors.textMuted,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
            if (subLabel != null) {
                Text(
                    subLabel,
                    color = if (enabled) FeudColors.deepNavy.copy(alpha = 0.75f) else FeudColors.textMuted,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
