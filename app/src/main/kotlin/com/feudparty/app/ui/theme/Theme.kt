package com.feudparty.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val Navy = Color(0xFF1B2A4A)
private val Gold = Color(0xFFF5B841)
private val Team1 = Color(0xFF2E7D32)
private val Team2 = Color(0xFF1565C0)

private val LightColors = lightColorScheme(
    primary = Navy,
    secondary = Gold,
    tertiary = Team1
)

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = Navy,
    secondary = Gold,
    tertiary = Team2
)

/** ألوان الفرق — نفس اللون بكل الشاشات حتى يميّز اللاعب فريقه بسرعة. */
object TeamColors {
    val team1 = Team1
    val team2 = Team2
}

/**
 * التطبيق عربي فقط بهاد الإصدار، فمنثبّت اتجاه الواجهة RTL بدل ما نتكل
 * على لغة الجهاز.
 */
@Composable
fun FeudPartyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    }
}
