package com.feudparty.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.ar
import com.feudparty.app.ui.theme.FeudColors

/** عدّاد الثواني — بيصير أحمر بآخر ٣ ثواني. */
@Composable
fun Countdown(
    seconds: Int,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val urgent = seconds in 1..3
    val color by animateColorAsState(
        targetValue = if (urgent) FeudColors.pink else FeudColors.gold,
        animationSpec = tween(200),
        label = "countdownColor"
    )

    CartoonSurface(
        modifier = modifier,
        color = color,
        borderWidth = 4.dp,
        corner = 14.dp,
        shadow = 5.dp
    ) {
        Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
            Text(
                seconds.ar(),
                color = FeudColors.ink,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
