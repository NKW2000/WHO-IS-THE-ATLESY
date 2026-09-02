package com.feudparty.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp

/**
 * ألوان المسرح — اللعبة كلها بخلفية غامقة وذهبي، زي استوديو البرنامج.
 * ما في وضع فاتح: اللوح لازم يبان على شاشة بغرفة معتمة.
 */
object FeudColors {
    val deepNavy = Color(0xFF060C1D)
    val navy = Color(0xFF0C1A38)
    val panel = Color(0xFF122a57)
    val panelDark = Color(0xFF0A1B3C)
    val gold = Color(0xFFF6C445)
    val goldDim = Color(0xFFB4862A)
    val strike = Color(0xFFE23A3A)
    val text = Color(0xFFF3F6FF)
    val textMuted = Color(0xFF93A7CE)
    val team1 = Color(0xFF35D6A0)
    val team2 = Color(0xFF5AA9FF)
}

/** ألوان الفرق — نفس اللون بكل الشاشات حتى يميّز اللاعب فريقه بسرعة. */
object TeamColors {
    val team1 = FeudColors.team1
    val team2 = FeudColors.team2
}

object FeudBrushes {
    /** خلفية المسرح: ضوء كشّاف من فوق وعتمة عالأطراف. */
    val stage = Brush.verticalGradient(
        0f to Color(0xFF16305F),
        0.45f to FeudColors.navy,
        1f to FeudColors.deepNavy
    )

    val tile = Brush.verticalGradient(
        listOf(Color(0xFF1B3F7F), Color(0xFF0D2050))
    )

    val tileRevealed = Brush.verticalGradient(
        listOf(Color(0xFF2559A8), Color(0xFF12356F))
    )

    val goldBar = Brush.horizontalGradient(
        listOf(FeudColors.goldDim, FeudColors.gold, FeudColors.goldDim)
    )
}

private val FeudTypography = Typography(
    displayMedium = TextStyle(fontWeight = FontWeight.Black, fontSize = 44.sp, letterSpacing = 0.sp),
    displaySmall = TextStyle(fontWeight = FontWeight.Black, fontSize = 34.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 27.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.6.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.4.sp)
)

private val FeudColorScheme = darkColorScheme(
    primary = FeudColors.gold,
    onPrimary = FeudColors.deepNavy,
    secondary = FeudColors.team2,
    background = FeudColors.deepNavy,
    onBackground = FeudColors.text,
    surface = FeudColors.panel,
    onSurface = FeudColors.text,
    surfaceVariant = FeudColors.panelDark,
    onSurfaceVariant = FeudColors.textMuted,
    error = FeudColors.strike
)

/**
 * التطبيق عربي فقط بهاد الإصدار، فمنثبّت اتجاه الواجهة RTL بدل ما نتكل
 * على لغة الجهاز.
 */
@Composable
fun FeudPartyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = FeudColorScheme, typography = FeudTypography) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    }
}
