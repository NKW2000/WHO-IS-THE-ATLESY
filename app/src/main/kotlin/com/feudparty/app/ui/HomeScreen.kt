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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.BrandLogo
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

/** قياس واحد لكل أزرار الرئيسية — نفس الارتفاع والحدّ والزوايا والظل. */
private val CARD_HEIGHT = 104.dp
private val CARD_GAP = 12.dp
private val CARD_CORNER = 22.dp
private val CARD_BORDER = 5.dp
private val CARD_SHADOW = 7.dp

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

    if (portrait) {
        // طولي: نفس كرت التصميم — شريط ألوان فوق، العلامة بالنص، وتحت
        // زر الاستضافة الكبير وتحته زرّين نصّين.
        PortraitHome(
            onHostClick = onHostClick,
            onJoinClick = onJoinClick,
            onSettingsClick = onSettingsClick
        )
        return
    }

    StageBackgroundHost {
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

/** الرئيسية بالوضع الطولي — مطابقة لكرت التصميم. */
@Composable
private fun PortraitHome(
    onHostClick: () -> Unit,
    onJoinClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(FeudColors.stageAlt, FeudColors.stage, FeudColors.panelDark),
                    radius = 900f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 26.dp)
        ) {
            // شريط الألوان فوق.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            ) {
                listOf(FeudColors.gold, FeudColors.pink, FeudColors.teal).forEach { color ->
                    Box(
                        modifier = Modifier
                            .width(46.dp)
                            .height(8.dp)
                            .background(color, RoundedCornerShape(999.dp))
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                BrandLogo(
                    em = 46.dp,
                    tagline = "لعبة عائلية · فريقين · جهاز لكل لاعب"
                )
            }

            // زر الاستضافة الكبير — نفس الحدّ والزوايا والظل تبع الزرّين
            // اللي تحته، وارتفاعه ثابت حتى ما يتغيّر مع طول السطر.
            CartoonSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CARD_HEIGHT),
                color = FeudColors.lime,
                borderWidth = CARD_BORDER,
                corner = CARD_CORNER,
                shadow = CARD_SHADOW,
                onClick = onHostClick
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "استضافة لعبة",
                            color = FeudColors.ink,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "افتح غرفة وخلّي اللاعبين يفوتوا",
                            color = FeudColors.ink.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(FeudColors.ink, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "‹",
                            color = FeudColors.lime,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }

            Spacer(Modifier.height(CARD_GAP))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CARD_GAP)
            ) {
                SmallHomeCard(
                    title = "انضمام كلاعب",
                    subtitle = "اكتب اسمك واختار غرفة",
                    color = FeudColors.teal,
                    modifier = Modifier
                        .weight(1f)
                        .height(CARD_HEIGHT),
                    onClick = onJoinClick
                )
                SmallHomeCard(
                    title = "الإعدادات",
                    subtitle = "استورد بنك أسئلتك",
                    color = FeudColors.gold,
                    modifier = Modifier
                        .weight(1f)
                        .height(CARD_HEIGHT),
                    onClick = onSettingsClick
                )
            }
        }
    }
}

/** كرت صغير بعنوان وسطر — الاتنين تحت زر الاستضافة، بنفس قياس الكبير. */
@Composable
private fun SmallHomeCard(
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    CartoonSurface(
        modifier = modifier,
        color = color,
        borderWidth = CARD_BORDER,
        corner = CARD_CORNER,
        shadow = CARD_SHADOW,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                title,
                color = FeudColors.ink,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                color = FeudColors.ink.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
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
        borderWidth = CARD_BORDER,
        corner = CARD_CORNER,
        shadow = CARD_SHADOW,
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
