package com.feudparty.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feudparty.app.ui.theme.DisplayFont
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

/**
 * علامة «مين الأطليسي» زي ما هي بملف التصميم (Brand Logo):
 *
 * - **الشارة**: قرص ذهبي بحدّ حبر ٠٫٠٩٣em وظل صلب ٠٫٠٦٨×٠٫٠٨٧em، وجوّاته
 *   «؟» كريمية بحجم ٠٫٦٢em محدودة بحبر ٠٫٠٥em ومايلة ٧ درجات.
 * - **الاسم**: تلات طبقات فوق بعض — طبقة مزاحة ٠٫٠٦٢×٠٫٠٨٨em (الظل)،
 *   طبقة حدّ حبر ٠٫١٤٥em، وفوقهن التعبئة الذهبية.
 * - **الشريط**: ذهبي بعرض ٤٫٥em وارتفاع ٠٫١٥em بحدّ حبر.
 *
 * كل المقاسات نسبة لـ [em] — نفس فكرة `font-size` بالتصميم.
 */
private const val WORDMARK = "مين الأطليسي"

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    em: Dp = 64.dp,
    tagline: String? = null,
    showBar: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(em * 0.19f)
    ) {
        BrandBadge(em = em)
        BrandWordmark(em = em)
        if (showBar) BrandBar(em = em)
        if (tagline != null) {
            Text(
                tagline,
                color = FeudColors.cream,
                style = TextStyle(
                    fontFamily = com.feudparty.app.ui.theme.BodyFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = em.toSp(0.16f),
                    letterSpacing = em.toSp(0.0096f)
                )
            )
        }
    }
}

/** نفس العلامة بس بصف واحد — الشارة جنب الاسم. */
@Composable
fun BrandLogoRow(
    modifier: Modifier = Modifier,
    em: Dp = 44.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(em * 0.36f)
    ) {
        BrandBadge(em = em)
        BrandWordmark(em = em)
    }
}

/** الشارة لحالها — بتستعمل بالمقدمة وبالأماكن الضيقة. */
@Composable
fun BrandBadge(em: Dp, modifier: Modifier = Modifier) {
    val shadowX = em * 0.068f
    val shadowY = em * 0.087f

    Box(
        modifier = modifier
            .size(em)
            .drawBehind {
                drawCircle(
                    color = FeudColors.ink,
                    radius = size.minDimension / 2f,
                    center = Offset(
                        size.width / 2f + shadowX.toPx(),
                        size.height / 2f + shadowY.toPx()
                    )
                )
            }
            .background(FeudColors.gold, CircleShape)
            .border(em * 0.093f, FeudColors.ink, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        StrokedText(
            text = "؟",
            fontSize = em * 0.62f,
            fill = FeudColors.cream,
            strokeWidth = em * 0.05f,
            modifier = Modifier
                .rotate(-7f)
                .offset(y = em * 0.02f)
        )
    }
}

/** الاسم بطبقاته التلاتة. */
@Composable
fun BrandWordmark(em: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // طبقة الظل — نفس الاسم مزاح ومحدود بالحبر.
        StrokedText(
            text = WORDMARK,
            fontSize = em,
            fill = FeudColors.ink,
            strokeWidth = em * 0.145f,
            modifier = Modifier.offset(x = -em * 0.062f, y = em * 0.088f)
        )
        // طبقة الحدّ.
        StrokedText(
            text = WORDMARK,
            fontSize = em,
            fill = FeudColors.ink,
            strokeWidth = em * 0.145f
        )
        // التعبئة الذهبية فوق.
        Text(
            WORDMARK,
            color = FeudColors.gold,
            style = wordStyle(em),
            maxLines = 1,
            softWrap = false
        )
    }
}

/** سطر من الاسم بالحبر والظل — بينستعمل بالمقدمة الطولية بسطرين. */
@Composable
fun BrandWordLine(text: String, em: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        StrokedText(
            text = text,
            fontSize = em,
            fill = FeudColors.ink,
            strokeWidth = em * 0.145f,
            modifier = Modifier.offset(x = -em * 0.062f, y = em * 0.088f)
        )
        StrokedText(
            text = text,
            fontSize = em,
            fill = FeudColors.gold,
            strokeWidth = em * 0.145f
        )
    }
}

@Composable
fun BrandBar(em: Dp, modifier: Modifier = Modifier) {
    val shadowX = em * 0.058f
    val shadowY = em * 0.068f
    val corner = em * 0.1f

    Box(
        modifier = modifier
            .width(em * 4.5f)
            .height(em * 0.15f)
            .drawBehind {
                drawRoundRect(
                    color = FeudColors.ink,
                    topLeft = Offset(-shadowX.toPx(), shadowY.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(corner.toPx())
                )
            }
            .background(FeudColors.gold, RoundedCornerShape(corner))
            .border(em * 0.048f, FeudColors.ink, RoundedCornerShape(corner))
    )
}

/** نص محدود بحبر: منرسمه مرتين — مرة حدّ ومرة تعبئة. */
@Composable
fun StrokedText(
    text: String,
    fontSize: Dp,
    fill: Color,
    strokeWidth: Dp,
    modifier: Modifier = Modifier
) {
    val strokePx = with(LocalDensity.current) { strokeWidth.toPx() }
    Box(modifier = modifier) {
        Text(
            text,
            color = FeudColors.ink,
            style = wordStyle(fontSize).copy(drawStyle = Stroke(width = strokePx)),
            maxLines = 1,
            softWrap = false
        )
        Text(
            text,
            color = fill,
            style = wordStyle(fontSize),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun wordStyle(fontSize: Dp) = TextStyle(
    fontFamily = DisplayFont,
    fontWeight = FontWeight.ExtraBold,
    fontSize = fontSize.toSp(1f),
    lineHeight = fontSize.toSp(1.05f)
)

@Composable
private fun Dp.toSp(factor: Float) = with(LocalDensity.current) { (this@toSp * factor).toSp() }

@Preview(showBackground = true, backgroundColor = 0xFF1C0C36, widthDp = 500, heightDp = 320)
@Composable
private fun BrandLogoPreview() {
    FeudPartyTheme {
        Box(modifier = Modifier.size(500.dp, 320.dp), contentAlignment = Alignment.Center) {
            BrandLogo(em = 52.dp, tagline = "لعبة عائلية · فريقين · جهاز لكل لاعب")
        }
    }
}
