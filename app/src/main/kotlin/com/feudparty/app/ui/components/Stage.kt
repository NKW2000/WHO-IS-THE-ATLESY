package com.feudparty.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudBrushes
import com.feudparty.app.ui.theme.FeudColors

/**
 * الأساس البصري لكل الشاشات: خلفية بنفسجية بنقط، وشريط ألوان متحرك فوق.
 */
@Composable
fun StageBackground(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FeudBrushes.stage)
            .dotGrid()
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(contentPadding), content = content)
    }
}

/** نقط خفيفة عالخلفية — نفس تكستشر التصميم. */
private fun Modifier.dotGrid(
    color: Color = Color.White.copy(alpha = 0.07f),
    spacing: Dp = 15.dp
): Modifier = drawBehind {
    val step = spacing.toPx()
    val radius = 1.2f
    var y = 0f
    while (y < size.height) {
        var x = 0f
        while (x < size.width) {
            drawCircle(color, radius, Offset(x, y))
            x += step
        }
        y += step
    }
}

/**
 * السطح الكرتوني: حد أسود سميك + ظل صلب مزاح (مش elevation).
 */
@Composable
fun CartoonSurface(
    modifier: Modifier = Modifier,
    color: Color = FeudColors.stageAlt,
    borderWidth: Dp = 5.dp,
    corner: Dp = 20.dp,
    shadow: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val shape: Shape = RoundedCornerShape(corner)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // الضغط بينزّل السطح على ظله — نفس إحساس الأزرار بالتصميم.
    val drop = if (pressed && onClick != null && enabled) shadow else 0.dp

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = -shadow, y = shadow)
                .background(FeudColors.ink, shape)
        )
        Box(
            modifier = Modifier
                .offset(x = -drop, y = drop)
                .background(color, shape)
                .border(borderWidth, FeudColors.ink, shape)
                .let {
                    if (onClick != null) {
                        it.clickable(
                            enabled = enabled,
                            interactionSource = interaction,
                            indication = null,
                            onClick = onClick
                        )
                    } else {
                        it
                    }
                },
            content = content
        )
    }
}

/** لوح كريمي — بيستعمل للسؤال وللبطاقات الفاتحة. */
@Composable
fun GoldPanel(
    modifier: Modifier = Modifier,
    accent: Color = FeudColors.cream,
    content: @Composable BoxScope.() -> Unit
) = CartoonSurface(modifier = modifier, color = accent, corner = 22.dp, shadow = 8.dp, content = content)

@Composable
fun GoldDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(4.dp)
            .background(FeudColors.ink, RoundedCornerShape(2.dp))
    )
}

/** زر أساسي — ذهبي كرتوني. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = FeudColors.gold
) {
    CartoonSurface(
        modifier = modifier,
        color = if (enabled) color else FeudColors.panelDark,
        corner = 18.dp,
        shadow = 6.dp,
        onClick = onClick,
        enabled = enabled
    ) {
        Text(
            text,
            color = if (enabled) FeudColors.ink else FeudColors.outlineSoft,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp)
        )
    }
}

/** زر ثانوي — بلون مخصص (فيروزي افتراضياً). */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = FeudColors.teal
) = PrimaryButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    color = accent
)

/** شارة صغيرة مدوّرة. */
@Composable
fun Pill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    textColor: Color = FeudColors.ink
) {
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(999.dp))
            .border(3.dp, FeudColors.ink, RoundedCornerShape(999.dp))
            .padding(horizontal = 16.dp, vertical = 7.dp)
    ) {
        Text(text, color = textColor, style = MaterialTheme.typography.labelLarge)
    }
}
