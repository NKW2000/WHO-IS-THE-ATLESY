package com.feudparty.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudBrushes
import com.feudparty.app.ui.theme.FeudColors

/** خلفية المسرح المشتركة لكل الشاشات — ضوء من فوق وعتمة على الأطراف. */
@Composable
fun StageBackground(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FeudBrushes.stage)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x33F6C445), Color(0x00000000)),
                        center = center.copy(y = 0f),
                        radius = size.width
                    ),
                    radius = size.width,
                    center = center.copy(y = 0f)
                )
            }
            .padding(contentPadding),
        content = content
    )
}

/** لوح ذهبي الإطار — الحاوية الأساسية لكل مجموعة عناصر. */
@Composable
fun GoldPanel(
    modifier: Modifier = Modifier,
    accent: Color = FeudColors.goldDim,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(FeudColors.panelDark, RoundedCornerShape(18.dp))
            .border(BorderStroke(2.dp, accent), RoundedCornerShape(18.dp)),
        content = content
    )
}

/** الشريط الذهبي الرفيع اللي بيفصل بين أقسام الشاشة. */
@Composable
fun GoldDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(2.dp)
            .background(FeudBrushes.goldBar, RoundedCornerShape(1.dp))
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FeudColors.gold,
            contentColor = FeudColors.deepNavy,
            disabledContainerColor = FeudColors.panel,
            disabledContentColor = FeudColors.textMuted
        )
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = FeudColors.gold
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(2.dp, if (enabled) accent else FeudColors.panel),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = accent,
            disabledContentColor = FeudColors.textMuted
        )
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/** شارة صغيرة للحالة (المرحلة، المضاعف، الدور...). */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = FeudColors.gold
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(50))
            .border(1.dp, color.copy(alpha = 0.55f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, style = MaterialTheme.typography.labelLarge)
    }
}
