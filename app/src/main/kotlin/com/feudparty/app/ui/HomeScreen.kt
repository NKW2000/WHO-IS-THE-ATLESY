package com.feudparty.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.BrandLogo
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

/**
 * الشاشة الرئيسية: العلامة على لوح غامق زي كرت «شاشة البداية» بملف
 * التصميم، وجنبها تلات أزرار كبار — كل زر معه سطر بيقول شو بيصير لما
 * تدوسه. الأزرار بتاخد كل الارتفاع فما بتضل الشاشة فاضية.
 */
@Composable
fun HomeScreen(
    onHostClick: () -> Unit,
    onJoinClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val float = rememberInfiniteTransition(label = "home")
    val tilt by float.animateFloat(
        initialValue = -1.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(5_000), RepeatMode.Reverse),
        label = "tilt"
    )

    val portrait = isPortrait()

    StageBackgroundHost {
        if (portrait) {
            // طولي: العلامة فوق، والأزرار تحتها بمتناول الإصبع.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
            ) {
                BrandPlate(
                    tilt = tilt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(shortSide() * 0.82f)
                )
                HomeActions(
                    onHostClick = onHostClick,
                    onJoinClick = onJoinClick,
                    onSettingsClick = onSettingsClick,
                    itemModifier = Modifier.height(104.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrandPlate(
                    tilt = tilt,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                Spacer(Modifier.width(18.dp))

                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeActions(
                        onHostClick = onHostClick,
                        onJoinClick = onJoinClick,
                        onSettingsClick = onSettingsClick,
                        itemModifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** لوح العلامة — نفس الكرت بالوضعين، بس ارتفاعه بيتغيّر. */
@Composable
private fun BrandPlate(tilt: Float, modifier: Modifier = Modifier) {
    CartoonSurface(
        modifier = modifier.rotate(tilt * 0.35f),
        color = FeudColors.canvas,
        borderWidth = 5.dp,
        corner = 26.dp,
        shadow = 9.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandLogo(em = 38.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                "لعبة عائلية · فريقين · جهاز لكل لاعب",
                color = FeudColors.textMuted,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** الأزرار التلاتة — نفس الترتيب بالوضعين. */
@Composable
private fun ColumnScope.HomeActions(
    onHostClick: () -> Unit,
    onJoinClick: () -> Unit,
    onSettingsClick: () -> Unit,
    itemModifier: Modifier
) {
    HomeAction(
        title = "استضافة لعبة",
        subtitle = "افتح غرفة وخلّي اللاعبين يفوتوا",
        color = FeudColors.lime,
        ink = FeudColors.ink,
        modifier = itemModifier,
        onClick = onHostClick
    )
    HomeAction(
        title = "انضمام كلاعب",
        subtitle = "اكتب اسمك واختار غرفة",
        color = FeudColors.teal,
        ink = FeudColors.ink,
        modifier = itemModifier,
        onClick = onJoinClick
    )
    HomeAction(
        title = "الإعدادات",
        subtitle = "استورد بنك أسئلتك",
        color = FeudColors.gold,
        ink = FeudColors.ink,
        modifier = itemModifier,
        onClick = onSettingsClick
    )
}

/** زر رئيسي بعنوان وسطر شرح — بياخد ارتفاعه من العمود. */
@Composable
private fun HomeAction(
    title: String,
    subtitle: String,
    color: Color,
    ink: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    CartoonSurface(
        modifier = modifier.fillMaxWidth(),
        color = color,
        borderWidth = 4.dp,
        corner = 18.dp,
        shadow = 6.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = ink, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            Text(
                subtitle,
                color = ink.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** نفس خلفية باقي الشاشات، بحشوة أوسع شوي للرئيسية. */
@Composable
private fun StageBackgroundHost(content: @Composable () -> Unit) {
    com.feudparty.app.ui.components.StageBackground(
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) { content() }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun HomeScreenPreview() {
    FeudPartyTheme { HomeScreen(onHostClick = {}, onJoinClick = {}) }
}
