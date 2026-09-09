package com.feudparty.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.SpinningRays
import com.feudparty.app.ui.components.appear
import com.feudparty.app.ui.components.drop
import com.feudparty.app.ui.components.keys
import com.feudparty.app.ui.components.rememberShowClock
import com.feudparty.app.ui.components.shockRing
import com.feudparty.app.ui.components.slamRotation
import com.feudparty.app.ui.components.slamScale
import com.feudparty.app.ui.components.thump
import com.feudparty.app.ui.components.wipe
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

/**
 * افتتاحية الجولة — نفس مشهدَي التصميم بملف `مين الأطليسي - Play & Pass`:
 * أول شي «بداية الجولة» (اسم الجولة والمضاعف)، وبعدها «استعدوا» (اللاعبين
 * اللي عالمنصة بقطع مايل و«ضد» بالنص). بتنعرض عند المضيف بس — هو الشاشة
 * الكبيرة اللي بتشوفها الغرفة. أي لمسة بتخطّيها.
 */
private const val INTRO_SECONDS = 2.2f
private const val VERSUS_SECONDS = 2.6f

private val ORDINALS = listOf(
    "الأولى", "الثانية", "الثالثة", "الرابعة", "الخامسة",
    "السادسة", "السابعة", "الثامنة", "التاسعة", "العاشرة"
)
private val MULTIPLIER_WORDS = listOf("فردية", "مزدوجة", "ثلاثية", "رباعية", "خماسية")

fun roundOrdinal(round: Int): String = ORDINALS.getOrElse(round - 1) { round.ar() }

fun multiplierWord(multiplier: Int): String =
    MULTIPLIER_WORDS.getOrElse(multiplier - 1) { "×${multiplier.ar()}" }

@Composable
fun RoundOpening(
    round: Int,
    multiplier: Int,
    playerA: String?,
    playerB: String?,
    teamAName: String,
    teamBName: String,
    onDone: () -> Unit
) {
    val finish by rememberUpdatedState(onDone)
    // بدون لاعبين عالمنصة ما في «استعدوا» — بنكتفي ببداية الجولة.
    val hasVersus = !playerA.isNullOrBlank() && !playerB.isNullOrBlank()
    var stage by remember(round) { mutableIntStateOf(0) }

    LaunchedEffect(round, stage, hasVersus) {
        val wait = if (stage == 0) INTRO_SECONDS else VERSUS_SECONDS
        kotlinx.coroutines.delay((wait * 1000).toLong())
        if (stage == 0 && hasVersus) stage = 1 else finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { finish() }
    ) {
        if (stage == 0) {
            RoundIntroScreen(round = round, multiplier = multiplier)
        } else {
            VersusScreen(
                playerA = playerA.orEmpty(),
                playerB = playerB.orEmpty(),
                teamAName = teamAName,
                teamBName = teamBName
            )
        }
    }
}

/** «بداية الجولة»: قرص بينط، اسم الجولة بيهبط بضربة، ولافتة المضاعف بتكنس. */
@Composable
fun RoundIntroScreen(round: Int, multiplier: Int, modifier: Modifier = Modifier) {
    val t by rememberShowClock(key = round, cap = 6f)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val portrait = maxHeight > maxWidth
        val w = maxWidth
        val tall = maxHeight
        // بالطولي القياسات من العرض (زي كرت التصميم ٣٨٥×٧٧٠)، وبالأفقي
        // من الارتفاع.
        val h = if (portrait) w else minOf(maxHeight, maxWidth)
        SpinningRays(modifier = Modifier.fillMaxSize())

        // قرص بنفسجي بينط ورا الاسم.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = if (portrait) -tall * 0.02f else 0.dp)
                .size(if (portrait) w * 0.88f else h * 1.28f)
                .scale(thump(t, 0f))
                .clip(CircleShape)
                .background(FeudColors.stageAlt)
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = if (portrait) -tall * 0.045f else -h * 0.055f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "الجولة",
                color = FeudColors.cream,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .scale(thump(t, 0.12f, 0.38f))
                    .graphicsLayer { alpha = appear(t, 0.12f) }
            )
            Text(
                roundOrdinal(round),
                color = FeudColors.gold,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = with(LocalDensity.current) {
                        (if (portrait) w * 0.22f else h * 0.30f).toSp()
                    },
                    lineHeight = with(LocalDensity.current) {
                        (if (portrait) w * 0.24f else h * 0.33f).toSp()
                    }
                ),
                maxLines = 1,
                modifier = Modifier.graphicsLayer {
                    val s = slamScale(t, 0.06f)
                    scaleX = s
                    scaleY = s
                    rotationZ = slamRotation(t, 0.06f)
                    alpha = appear(t, 0.06f, 0.1f)
                }
            )
        }

        // لافتة المضاعف بتكنس من الجهة بعد ما يستقر الاسم.
        GoldBanner(
            text = "${multiplierWord(multiplier)} ×${multiplier.ar()}",
            width = w,
            height = if (portrait) tall * 0.104f else h * 0.183f,
            offsetFraction = wipe(t, 0.70f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = if (portrait) tall * 0.774f else (maxHeight - h * 0.183f) / 2f + h * 0.52f)
        )
    }
}

/** «استعدوا»: نصفين بقطع مايل، وبينهم «ضد» مع حلقة صدمة. */
@Composable
fun VersusScreen(
    playerA: String,
    playerB: String,
    teamAName: String,
    teamBName: String,
    modifier: Modifier = Modifier
) {
    val t by rememberShowClock(key = playerA + playerB, cap = 6f)

    // الهندسة بالتصميم يسار/يمين فيزيائي — منثبّت الاتجاه حتى تطابق.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val portrait = maxHeight > maxWidth
            val w = maxWidth
            val tall = maxHeight
            // القياسات من العرض بالطولي ومن الارتفاع بالأفقي.
            val basis = if (portrait) w else minOf(maxHeight, maxWidth)
            val density = LocalDensity.current
            SpinningRays(modifier = Modifier.fillMaxSize())

            val slide = keys(t, 0.12f, 0.64f, listOf(0f to 0.58f, 1f to 0f))
            val fade = appear(t, 0.12f, 0.2f)
            val seam = keys(t, 0.64f, 0.52f, listOf(0f to 0f, 0.7f to 1.06f, 1f to 1f))

            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                if (portrait) {
                    // فوق أخضر وتحت أزرق، والقطع مايل بالعرض.
                    val top = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(width, 0f)
                        lineTo(width, height * 0.58f)
                        lineTo(0f, height * 0.42f)
                        close()
                    }
                    val bottom = Path().apply {
                        moveTo(0f, height * 0.42f)
                        lineTo(width, height * 0.58f)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    translate(0f, -height * slide) {
                        drawPath(top, FeudColors.team1.copy(alpha = fade))
                    }
                    translate(0f, height * slide) {
                        drawPath(bottom, FeudColors.team2.copy(alpha = fade))
                    }
                    if (seam > 0f) {
                        val seamHeight = height * 0.021f
                        val slant = Math.toDegrees(
                            kotlin.math.atan2((0.16f * height).toDouble(), width.toDouble())
                        ).toFloat()
                        rotate(degrees = slant, pivot = center) {
                            drawRect(
                                color = FeudColors.ink,
                                topLeft = Offset(
                                    center.x - width * 0.64f * seam,
                                    center.y - seamHeight / 2f
                                ),
                                size = androidx.compose.ui.geometry.Size(
                                    width * 1.28f * seam,
                                    seamHeight
                                )
                            )
                        }
                    }
                } else {
                    val right = Path().apply {
                        moveTo(width * 0.58f, 0f)
                        lineTo(width, 0f)
                        lineTo(width, height)
                        lineTo(width * 0.42f, height)
                        close()
                    }
                    val left = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(width * 0.58f, 0f)
                        lineTo(width * 0.42f, height)
                        lineTo(0f, height)
                        close()
                    }
                    translate(width * slide, 0f) {
                        drawPath(right, FeudColors.team1.copy(alpha = fade))
                    }
                    translate(-width * slide, 0f) {
                        drawPath(left, FeudColors.team2.copy(alpha = fade))
                    }
                    if (seam > 0f) {
                        val seamWidth = width * 0.018f
                        val slant = Math.toDegrees(
                            kotlin.math.atan2((0.16f * width).toDouble(), height.toDouble())
                        ).toFloat()
                        rotate(degrees = slant, pivot = center) {
                            drawRect(
                                color = FeudColors.ink,
                                topLeft = Offset(
                                    center.x - seamWidth / 2f,
                                    center.y - height * 0.64f * seam
                                ),
                                size = androidx.compose.ui.geometry.Size(
                                    seamWidth,
                                    height * 1.28f * seam
                                )
                            )
                        }
                    }
                }
            }

            if (portrait) {
                val blockHeight = tall * 0.5f - basis * 0.24f
                NameHalf(
                    label = teamAName,
                    name = playerA,
                    ink = FeudColors.team1Ink,
                    nameColor = FeudColors.team1Ink,
                    nameSize = basis * 0.14f,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(blockHeight)
                        .offset(y = -tall * slide)
                        .graphicsLayer { alpha = fade }
                )
                NameHalf(
                    label = teamBName,
                    name = playerB,
                    ink = FeudColors.team2Ink,
                    nameColor = FeudColors.cream,
                    nameSize = basis * 0.14f,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(blockHeight)
                        .offset(y = tall * slide)
                        .graphicsLayer { alpha = fade }
                )
            } else {
                val halfWidth = maxWidth * 0.5f - basis * 0.2f
                val nameBasis = minOf(basis * 0.14f, halfWidth * 0.2f)
                NameHalf(
                    label = teamAName,
                    name = playerA,
                    ink = FeudColors.team1Ink,
                    nameColor = FeudColors.team1Ink,
                    nameSize = nameBasis,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(halfWidth)
                        .offset(x = w * slide)
                        .graphicsLayer { alpha = fade }
                )
                NameHalf(
                    label = teamBName,
                    name = playerB,
                    ink = FeudColors.team2Ink,
                    nameColor = FeudColors.cream,
                    nameSize = nameBasis,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(halfWidth)
                        .offset(x = -w * slide)
                        .graphicsLayer { alpha = fade }
                )
            }

            // حلقة الصدمة ثم شارة «ضد».
            val ring = shockRing(t, 0.82f)
            if (ring.alpha > 0f) {
                val ringSize = basis * 0.52f
                Canvas(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(ringSize * 2.2f)
                ) {
                    val stroke = with(density) { (ring.width * (basis.value / 385f)).dp.toPx() }
                    drawCircle(
                        color = FeudColors.gold.copy(alpha = ring.alpha),
                        radius = with(density) { ringSize.toPx() } / 2f * ring.scale,
                        style = Stroke(width = stroke.coerceAtLeast(1f))
                    )
                }
            }

            val badge = basis * 0.38f
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(badge)
                    .scale(thump(t, 0.82f))
                    .clip(CircleShape)
                    .background(FeudColors.gold)
                    .padding(badge * 0.06f)
                    .clip(CircleShape)
                    .background(FeudColors.ink),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "ضد",
                    color = FeudColors.cream,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = with(density) { (badge * 0.36f).toSp() }
                    )
                )
            }
        }
    }
}

/** نص نصف الشاشة: اسم الفريق فوق واسم اللاعب كبير تحته. */
@Composable
private fun NameHalf(
    label: String,
    name: String,
    ink: Color,
    nameColor: Color,
    nameSize: Dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            label,
            color = ink,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            name,
            color = nameColor,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = with(density) { nameSize.toSp() },
                lineHeight = with(density) { (nameSize * 1.14f).toSp() }
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/** لافتة ذهبية بتكنس عرض الشاشة — نفس `dcWipe` بالتصميم. */
@Composable
fun GoldBanner(
    text: String,
    width: Dp,
    height: Dp,
    offsetFraction: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .offset(x = width * offsetFraction)
            .background(FeudColors.gold),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = FeudColors.ink,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = with(density) { (height * 0.52f).toSp() }
            ),
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun RoundIntroPreview() {
    FeudPartyTheme { RoundIntroScreen(round = 3, multiplier = 2) }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun VersusPreview() {
    FeudPartyTheme {
        VersusScreen(
            playerA = "سلمان",
            playerB = "نورة",
            teamAName = "نمور الشام",
            teamBName = "صقور البحر"
        )
    }
}
