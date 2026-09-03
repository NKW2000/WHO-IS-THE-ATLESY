package com.feudparty.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feudparty.app.ui.theme.FeudColors
import kotlinx.coroutines.delay

/** الخطوط الثلاثة — مربعات مدوّرة، الممتلئة وردية بظل صلب. */
@Composable
fun StrikeRow(
    strikes: Int,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    total: Int = 3
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(total) { index ->
            StrikeBox(filled = index < strikes, size = size)
        }
    }
}

@Composable
private fun StrikeBox(filled: Boolean, size: Dp) {
    // الضربة بتنزل بحركة slam: بتكبر وبترتد لمكانها.
    val scale by animateFloatAsState(
        targetValue = if (filled) 1f else 0.94f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "strikeScale"
    )
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .background(if (filled) FeudColors.pink else FeudColors.ink.copy(alpha = 0.4f), shape)
            .border(4.dp, FeudColors.ink, shape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "✕",
            color = if (filled) Color.White else Color(0xFF5C4A8C),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/** علامة X مفردة — بتستعمل بشاشات تانية. */
@Composable
fun StrikeMark(color: Color, modifier: Modifier = Modifier, size: Dp = 38.dp) {
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = modifier
            .size(size)
            .background(color, shape)
            .border(4.dp, FeudColors.ink, shape),
        contentAlignment = Alignment.Center
    ) {
        Text("✕", color = Color.White, style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * X كبير بينزل على كل الشاشة لحظة الخطأ — أوضح إشي بغرفة مليانة ناس.
 */
@Composable
fun StrikeFlash(strikes: Int, modifier: Modifier = Modifier) {
    var lastSeen by remember { mutableIntStateOf(strikes) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(strikes) {
        if (strikes > lastSeen) {
            visible = true
            delay(650)
            visible = false
        }
        lastSeen = strikes
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 2.4f, animationSpec = tween(220)) + fadeIn(tween(120)),
        exit = fadeOut(tween(200)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "✕",
                color = FeudColors.pink,
                fontSize = 220.sp,
                style = MaterialTheme.typography.displayLarge
            )
        }
    }
}
