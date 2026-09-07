package com.feudparty.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.Fireworks
import com.feudparty.app.ui.components.Wordmark
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * مقدمة اللعبة — نفس سيناريو الموشن اللي بملف التصميم:
 * الفريقين بيندفعوا من الجنبين وبيتصادموا بالنص، بتطلع علامة السؤال،
 * وبعدها بتتبنى العلامة (اللوح + الاسم + الشريط الذهبي) وبتفرقع الألعاب
 * النارية. أي لمسة بتخطّيها.
 */
@Composable
fun IntroScreen(onDone: () -> Unit) {
    val finish by rememberUpdatedState(onDone)

    val slam = remember { Animatable(1f) }      // الفريقين جايين من الجنب
    val shock = remember { Animatable(0f) }     // موجة الصدمة
    val mark = remember { Animatable(0f) }      // علامة السؤال
    val part = remember { Animatable(0f) }      // الفريقين بيفترقوا
    val logo = remember { Animatable(0f) }      // العلامة بتتبنى
    val burst = remember { Animatable(0f) }     // الألعاب النارية

    LaunchedEffect(Unit) {
        launch {
            slam.animateTo(0f, tween(420, easing = FastOutSlowInEasing))
            shock.animateTo(1f, tween(320, easing = LinearEasing))
        }
        delay(380)
        mark.animateTo(1f, tween(260, easing = EaseOutBack))
        delay(360)
        launch { part.animateTo(1f, tween(420, easing = FastOutSlowInEasing)) }
        launch { mark.animateTo(0f, tween(200)) }
        logo.animateTo(1f, tween(520, easing = EaseOutBack))
        burst.animateTo(1f, tween(200))
        delay(900)
        finish()
    }

    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FeudColors.gold)
            .clickable(interactionSource = interaction, indication = null) { finish() },
        contentAlignment = Alignment.Center
    ) {
        // الفريقين: أخضر وأزرق بيتصادموا بالنص وبعدين بيفترقوا.
        TeamSlab(
            color = FeudColors.team1,
            offsetX = (-520 * slam.value - 620 * part.value).dp,
            rotation = -6f * part.value
        )
        TeamSlab(
            color = FeudColors.team2,
            offsetX = (520 * slam.value + 620 * part.value).dp,
            rotation = 6f * part.value
        )

        // موجة الصدمة بلحظة التصادم.
        if (shock.value > 0f && shock.value < 1f) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(0.4f + shock.value * 3.2f)
                    .alpha(1f - shock.value)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(FeudColors.cream.copy(alpha = 0.35f))
            )
        }

        // علامة السؤال الكبيرة.
        if (mark.value > 0.01f) {
            Text(
                "؟",
                color = FeudColors.cream,
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .scale(mark.value * 2.6f)
                    .rotate(-7f)
                    .graphicsLayer { shadowElevation = 0f }
            )
        }

        // العلامة الكاملة على لوح غامق، زي آخر لقطة بالموشن.
        if (logo.value > 0.01f) {
            Box(
                modifier = Modifier
                    .scale(0.86f + logo.value * 0.14f)
                    .alpha(logo.value.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(28.dp))
                    .background(FeudColors.canvas)
                    .offset(y = 0.dp)
            ) {
                Wordmark(
                    showTags = false,
                    modifier = Modifier
                        .background(Color.Transparent)
                        .size(width = 560.dp, height = 150.dp)
                )
            }
        }

        if (burst.value > 0f) Fireworks(bursts = 4, cycleMillis = 1800)
    }
}

@Composable
private fun TeamSlab(color: Color, offsetX: androidx.compose.ui.unit.Dp, rotation: Float) {
    Box(
        modifier = Modifier
            .offset(x = offsetX)
            .rotate(rotation)
            .size(width = 190.dp, height = 118.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(FeudColors.ink)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (-5).dp, y = (-5).dp)
                .clip(RoundedCornerShape(22.dp))
                .background(color)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(widthDp = 880, heightDp = 420)
@Composable
private fun IntroScreenPreview() {
    FeudPartyTheme { IntroScreen(onDone = {}) }
}
