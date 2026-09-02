package com.feudparty.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudColors
import kotlinx.coroutines.delay

/** صف الأخطاء (X) — ٣ أخطاء بتفتح فرصة السرقة للفريق التاني. */
@Composable
fun StrikeRow(
    strikes: Int,
    modifier: Modifier = Modifier,
    slots: Int = 3,
    size: Dp = 34.dp
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(slots) { index ->
            val active = index < strikes
            val scale by animateFloatAsState(
                targetValue = if (active) 1f else 0.78f,
                animationSpec = tween(220),
                label = "strike"
            )
            StrikeMark(
                color = if (active) FeudColors.strike else FeudColors.textMuted.copy(alpha = 0.25f),
                modifier = Modifier
                    .size(size)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
            )
        }
    }
}

@Composable
fun StrikeMark(color: Color, modifier: Modifier = Modifier, strokeWidth: Dp = 5.dp) {
    Canvas(modifier = modifier) {
        val inset = size.minDimension * 0.18f
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        drawLine(
            color = color,
            start = Offset(inset, inset),
            end = Offset(size.width - inset, size.height - inset),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width - inset, inset),
            end = Offset(inset, size.height - inset),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}

/**
 * الـ X الكبير اللي بيغطي الشاشة لحظة الخطأ — بيظهر لما يزيد عدد الأخطاء
 * وبيروح لحاله بعد أقل من ثانية.
 */
@Composable
fun StrikeFlash(strikes: Int, modifier: Modifier = Modifier) {
    var visible by remember { mutableIntStateOf(0) }
    var previous by remember { mutableIntStateOf(strikes) }

    LaunchedEffect(strikes) {
        if (strikes > previous) {
            visible = strikes
            delay(850)
            visible = 0
        }
        previous = strikes
    }

    val shown = visible
    if (shown == 0) return

    val alpha by animateFloatAsState(targetValue = 1f, animationSpec = tween(120), label = "flashAlpha")

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(shown) {
                StrikeMark(
                    color = FeudColors.strike.copy(alpha = alpha),
                    strokeWidth = 16.dp,
                    modifier = Modifier.size(if (shown >= 3) 96.dp else 130.dp)
                )
            }
        }
    }
}
