package com.feudparty.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * التطبيق بيشتغل بالوضعين: الجهاز الأفقي (شاشة الغرفة الكبيرة) والجهاز
 * الطولي (موبايل بالإيد). كل شاشة بتبني نفس المحتوى بترتيبين:
 *
 * - **أفقي**: أعمدة جنب بعض، وكل إشي داخل الشاشة بدون تمرير.
 * - **طولي**: عمود واحد بيتمرّر، والأزرار الأساسية ملزوقة تحت بمتناول
 *   الإصبع.
 *
 * القياسات النسبية (خط كبير، دوائر، لوحات) بتتحسب من [shortSide] — أقصر
 * بُعد بالشاشة — حتى ما تنفجر بالوضع الطولي.
 */
@Composable
@ReadOnlyComposable
fun isPortrait(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenHeightDp > configuration.screenWidthDp
}

/** أقصر بُعد بالشاشة بالـ dp. */
@Composable
@ReadOnlyComposable
fun shortSide(): Dp {
    val configuration = LocalConfiguration.current
    return minOf(configuration.screenWidthDp, configuration.screenHeightDp).dp
}

/** حشوة الشاشة — أوسع بالأفقي وأضيق بالطولي. */
@Composable
@ReadOnlyComposable
fun stagePadding(): PaddingValues =
    if (isPortrait()) {
        PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    } else {
        PaddingValues(horizontal = 20.dp, vertical = 14.dp)
    }
