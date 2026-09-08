package com.feudparty.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

@Composable
fun PermissionExplanationScreen(
    permanentlyDenied: Boolean = false,
    onRequestClick: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val portrait = isPortrait()

    StageBackground(contentPadding = stagePadding()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!portrait) {
                SignalBadge()
                Spacer(Modifier.width(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "بدنا صلاحية قبل ما نبلّش",
                    color = FeudColors.gold,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (permanentlyDenied) {
                        "رفضت الصلاحيات نهائياً، فلازم تفتحها من إعدادات النظام حتى " +
                            "تقدر تلعب — التطبيق ما بيشتغل بدونها."
                    } else {
                        "التطبيق بيربط أجهزتكم مع بعض مباشرة عبر البلوتوث والواي فاي — " +
                            "بدون إنترنت وبدون سيرفر خارجي، وما منجمع ولا منبعت أي بيانات لبرا."
                    },
                    color = FeudColors.textSoft,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PermissionCard(
                        title = "بلوتوث",
                        subtitle = "لإيجاد الأجهزة القريبة",
                        accent = FeudColors.teal,
                        modifier = Modifier.weight(1f)
                    )
                    PermissionCard(
                        title = "واي فاي قريب",
                        subtitle = "لنقل حالة اللعبة",
                        accent = FeudColors.lime,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(18.dp))
                PrimaryButton(
                    text = if (permanentlyDenied) "افتح الإعدادات" else "منح الصلاحيات",
                    onClick = if (permanentlyDenied) onOpenSettings else onRequestClick,
                    modifier = if (portrait) Modifier.fillMaxWidth() else Modifier.width(300.dp)
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    CartoonSurface(
        modifier = modifier,
        color = FeudColors.stageAlt,
        borderWidth = 4.dp,
        corner = 16.dp,
        shadow = 5.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(title, color = accent, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = FeudColors.textMuted, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** أيقونة الإشارة — دوائر بتتوسّع حواليها زي البث. */
@Composable
private fun SignalBadge() {
    val transition = rememberInfiniteTransition(label = "signal")
    val outer by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.7f,
        animationSpec = infiniteRepeatable(tween(2_200), RepeatMode.Restart),
        label = "outerRing"
    )
    val inner by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.7f,
        animationSpec = infiniteRepeatable(tween(2_200, delayMillis = 700), RepeatMode.Restart),
        label = "innerRing"
    )
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1_300), RepeatMode.Reverse),
        label = "bob"
    )

    Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(outer)
                .border(5.dp, FeudColors.teal.copy(alpha = (1.7f - outer).coerceIn(0f, 1f)), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(110.dp)
                .scale(inner)
                .border(5.dp, FeudColors.pink.copy(alpha = (1.7f - inner).coerceIn(0f, 1f)), CircleShape)
        )
        CartoonSurface(
            modifier = Modifier
                .width(118.dp)
                .offset(y = -bob.dp),
            color = FeudColors.gold,
            corner = 32.dp,
            shadow = 8.dp
        ) {
            Text(
                "⌁",
                color = FeudColors.ink,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun PermissionExplanationScreenPreview() {
    FeudPartyTheme { PermissionExplanationScreen(onRequestClick = {}) }
}
