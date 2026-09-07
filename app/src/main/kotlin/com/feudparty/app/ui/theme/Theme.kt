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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.feudparty.app.R

/**
 * ألوان التصميم الكرتوني: حدود سودا سميكة، ظلال صلبة، وألوان مشبعة على
 * خلفية بنفسجية. [ink] هو لون الحد والظل بكل مكان.
 */
object FeudColors {
    val ink = Color(0xFF140626)
    val canvas = Color(0xFF170A31)
    val stage = Color(0xFF2A1258)
    val stageAlt = Color(0xFF3A1C6E)
    val panelDark = Color(0xFF241048)
    val gold = Color(0xFFFFD23F)
    val pink = Color(0xFFFF5470)
    val teal = Color(0xFF2BE0D6)
    val lime = Color(0xFF9BE564)
    val cream = Color(0xFFFFF6E5)

    val team1 = Color(0xFF37C46B)
    val team1Ink = Color(0xFF0B2E18)
    val team2 = Color(0xFF2D9CFF)
    val team2Ink = Color(0xFF08213D)

    val text = cream
    val textSoft = Color(0xFFDCCEFB)
    val textMuted = Color(0xFFC9B6EE)
    val textFaint = Color(0xFF8B76C4)
    val outlineSoft = Color(0xFF6E5A9C)

    // أسماء قديمة بيستعملها باقي الكود.
    val deepNavy = canvas
    val strike = pink
    val goldDim = Color(0xFFA78FD8)
}

/** ألوان الفرق — نفس اللون بكل الشاشات حتى يميّز اللاعب فريقه بسرعة. */
object TeamColors {
    val team1 = FeudColors.team1
    val team2 = FeudColors.team2
}

object FeudBrushes {
    val stage = Brush.verticalGradient(
        listOf(FeudColors.stageAlt, FeudColors.stage, FeudColors.canvas)
    )

    /** الشريط المخطط اللي فوق الشاشات — ذهبي/وردي/فيروزي. */
    val stripes = listOf(FeudColors.gold, FeudColors.pink, FeudColors.teal)
}

/** خط العناوين — Baloo Bhaijaan 2. */
val DisplayFont = FontFamily(
    Font(R.font.baloo_bhaijaan2_medium, FontWeight.Medium),
    Font(R.font.baloo_bhaijaan2_bold, FontWeight.Bold),
    Font(R.font.baloo_bhaijaan2_extrabold, FontWeight.ExtraBold)
)

/** خط النصوص — Tajawal. */
val BodyFont = FontFamily(
    Font(R.font.tajawal_medium, FontWeight.Medium),
    Font(R.font.tajawal_bold, FontWeight.Bold),
    Font(R.font.tajawal_extrabold, FontWeight.ExtraBold)
)

private fun display(size: Int, lineHeight: Int = (size * 1.2).toInt()) = TextStyle(
    fontFamily = DisplayFont,
    fontWeight = FontWeight.ExtraBold,
    fontSize = size.sp,
    lineHeight = lineHeight.sp
)

private fun body(size: Int, weight: FontWeight = FontWeight.Medium) = TextStyle(
    fontFamily = BodyFont,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size * 1.45).toInt().sp
)

// المقاسات مضبوطة على شاشة تلفون أفقية: كل شي لازم يوقع بشاشة وحدة
// بدون تمرير، فالخط أصغر من مقاسات ماتيريال الافتراضية.
private val FeudTypography = Typography(
    displayLarge = display(42),
    displayMedium = display(34),
    displaySmall = display(28),
    headlineLarge = display(25),
    headlineMedium = display(22),
    headlineSmall = display(19),
    titleLarge = display(18),
    titleMedium = display(15),
    titleSmall = display(13),
    bodyLarge = body(13),
    bodyMedium = body(11),
    labelLarge = body(11, FontWeight.ExtraBold),
    labelMedium = body(10, FontWeight.Bold),
    labelSmall = body(9, FontWeight.Bold)
)

private val FeudColorScheme = darkColorScheme(
    primary = FeudColors.gold,
    onPrimary = FeudColors.ink,
    secondary = FeudColors.teal,
    onSecondary = FeudColors.ink,
    tertiary = FeudColors.pink,
    background = FeudColors.canvas,
    onBackground = FeudColors.text,
    surface = FeudColors.stage,
    onSurface = FeudColors.text,
    error = FeudColors.pink
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
