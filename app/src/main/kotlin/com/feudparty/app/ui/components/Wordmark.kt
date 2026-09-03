package com.feudparty.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feudparty.app.ui.theme.DisplayFont
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

/**
 * الوردمارك الكامل زي ملف التصميم: بلاطة ذهبية فيها «؟» فيروزية بظل حبر،
 * وجنبها لوحة الاسم مايلة، وتحتها شارتين.
 */
@Composable
fun Wordmark(
    modifier: Modifier = Modifier,
    tileSize: Dp = 104.dp,
    nameSize: Int = 44,
    showTags: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "wordmark")
    val wobble by transition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(5_000), RepeatMode.Reverse),
        label = "wobble"
    )

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        QuestionTile(size = tileSize, rotation = wobble)
        Spacer(Modifier.size(20.dp))
        Column {
            CartoonSurface(
                modifier = Modifier.rotate(-2f),
                color = FeudColors.gold,
                borderWidth = 6.dp,
                corner = 20.dp,
                shadow = 8.dp
            ) {
                Text(
                    "مين الأطليسي",
                    color = FeudColors.ink,
                    style = TextStyle(
                        fontFamily = DisplayFont,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        fontSize = nameSize.sp,
                        lineHeight = (nameSize * 1.15f).sp
                    ),
                    modifier = Modifier.padding(horizontal = 26.dp, vertical = 10.dp)
                )
            }
            if (showTags) {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill(
                        text = "لعبة عائلية",
                        color = FeudColors.pink,
                        textColor = Color.White
                    )
                    Pill(text = "بدون إنترنت", color = FeudColors.lime)
                }
            }
        }
    }
}

/** بلاطة العلامة — «؟» فيروزية وظلها حبر مزاح، نفس أيقونة التطبيق. */
@Composable
fun QuestionTile(
    size: Dp = 104.dp,
    rotation: Float = 0f,
    modifier: Modifier = Modifier
) {
    val glyph = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
        fontSize = (size.value * 0.62f).sp,
        lineHeight = (size.value * 0.62f).sp
    )
    CartoonSurface(
        modifier = modifier.rotate(rotation),
        color = FeudColors.gold,
        borderWidth = 6.dp,
        corner = size * 0.27f,
        shadow = 8.dp
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            // الظل الصلب للعلامة.
            Text(
                "؟",
                color = FeudColors.ink,
                style = glyph,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
            )
            Text("؟", color = FeudColors.teal, style = glyph, textAlign = TextAlign.Center)
        }
    }
}

@Preview(widthDp = 620, heightDp = 260, showBackground = true, backgroundColor = 0xFF2A1258)
@Composable
private fun WordmarkPreview() {
    FeudPartyTheme {
        Box(modifier = Modifier.padding(24.dp)) { Wordmark() }
    }
}
