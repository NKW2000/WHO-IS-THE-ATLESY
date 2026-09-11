package com.feudparty.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import com.feudparty.app.ui.components.blockSkin
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
import androidx.compose.runtime.State
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
import com.feudparty.app.ui.theme.FeudShape
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.GameState
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId

/**
 * افتتاحية الجولة — نفس مشهدَي التصميم بملف `مين الأطليسي - Play & Pass`:
 * أول شي «بداية الجولة» (اسم الجولة والمضاعف)، وبعدها «استعدوا» (اللاعبين
 * اللي عالمنصة بقطع مايل و«ضد» بالنص). بتنعرض عند المضيف وعند كل لاعب.
 *
 * ما بتنتخطّى: اللمسة بتنبلع وما بتوصل لشي تحتها — وإلا اللاعب بيتخطّاها
 * وبيضغط الزر قبل ما يخلص المشهد عند الباقيين.
 */
private const val INTRO_SECONDS = 1.9f
private const val VERSUS_SECONDS = 2.0f

/** توقيت «استعدوا»: الكروت، الرجّة، وشارة «ضد». */
private const val CARD_AT = 0.06f
private const val CARD_TIME = 0.44f
private const val SHAKE_AT = 0.44f
private const val BADGE_AT = 0.5f

private val ORDINALS = listOf(
    "الأولى", "الثانية", "الثالثة", "الرابعة", "الخامسة",
    "السادسة", "السابعة", "الثامنة", "التاسعة", "العاشرة"
)
private val MULTIPLIER_WORDS = listOf("فردية", "مزدوجة", "ثلاثية", "رباعية", "خماسية")

fun roundOrdinal(round: Int): String = ORDINALS.getOrElse(round - 1) { round.ar() }

fun multiplierWord(multiplier: Int): String =
    MULTIPLIER_WORDS.getOrElse(multiplier - 1) { "×${multiplier.ar()}" }

/**
 * الافتتاحية فوق اللوح — نفس الحالة بتوصل للمضيف وللاعبين، فكل جهاز
 * بيشغّل المشهد لحاله أول ما تبلّش جولة جديدة بالمواجهة، فبيطلعوا مع
 * بعض بفرق الشبكة بس. بتنعرض مرة وحدة لكل جولة على كل جهاز.
 */
@Composable
fun RoundOpeningOverlay(state: GameState?) {
    var openedRound by remember { mutableIntStateOf(-1) }
    if (state == null || !state.matchStarted || state.gameOver) return
    val roundIndex = state.currentQuestionIndex
    if (state.phase != RoundPhase.FACE_OFF || openedRound == roundIndex) return

    RoundOpening(
        round = roundIndex + 1,
        multiplier = state.multiplier,
        playerA = state.podiumPlayer(TeamId.TEAM_1)?.name,
        playerB = state.podiumPlayer(TeamId.TEAM_2)?.name,
        teamAName = state.teams[TeamId.TEAM_1]?.name.orEmpty(),
        teamBName = state.teams[TeamId.TEAM_2]?.name.orEmpty(),
        onDone = { openedRound = roundIndex }
    )
}

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
            // بتاكل اللمسة بدون ما تعمل شي — حاجز، مش زر تخطّي.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {}
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
        // القياس الأساسي من أقصر بُعد — هيك القرص بيضل دايرة كاملة جوّا
        // الكادر بالوضعين، وما بينقص منه ولا بيطلع بيضوي.
        val h = minOf(maxHeight, maxWidth)
        SpinningRays(modifier = Modifier.fillMaxSize())

        // قرص بنفسجي بينط ورا الاسم.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = if (portrait) -tall * 0.02f else 0.dp)
                .size(if (portrait) w * 0.88f else h * 0.82f)
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
                        (if (portrait) w * 0.22f else h * 0.20f).toSp()
                    },
                    lineHeight = with(LocalDensity.current) {
                        (if (portrait) w * 0.24f else h * 0.22f).toSp()
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
            height = if (portrait) tall * 0.104f else h * 0.16f,
            offsetFraction = wipe(t, 0.70f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = if (portrait) tall * 0.774f else tall * 0.76f)
        )
    }
}

/**
 * «استعدوا» — نفس شاشة التصميم بالوضعين: كرت لكل لاعب بينقذف لمكانه
 * وبينهم «ضد» مع حلقة صدمة.
 */
@Composable
fun VersusScreen(
    playerA: String,
    playerB: String,
    teamAName: String,
    teamBName: String,
    modifier: Modifier = Modifier
) {
    val clock = rememberShowClock(key = playerA + playerB, cap = 4f)
    VersusCards(
        playerA = playerA,
        playerB = playerB,
        teamAName = teamAName,
        teamBName = teamBName,
        clock = clock,
        modifier = modifier
    )
}

/**
 * «استعدوا — الأسماء» — كرت لكل لاعب بينقذف من فوق ومن تحت، رجّة كادر،
 * حلقة صدمة، وشارة «ضد» بتنط بالنص.
 *
 * الوقت بينقرأ جوّا `graphicsLayer` و`Canvas` بس — يعني كل فريم بيعيد
 * الرسم لحاله بدون ما يعيد تركيب الشاشة، فالحركة بتطلع ناعمة.
 */
@Composable
private fun VersusCards(
    playerA: String,
    playerB: String,
    teamAName: String,
    teamBName: String,
    clock: State<Float>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val w = maxWidth
        val tall = maxHeight
        val density = LocalDensity.current
        val span = minOf(w, tall * 0.62f)
        SpinningRays(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // رجّة الكادر بعد التصادم.
                    val time = clock.value
                    val shake = if (time in SHAKE_AT..(SHAKE_AT + 0.24f)) {
                        val p = (time - SHAKE_AT) / 0.24f
                        kotlin.math.sin(p * 20f) * (1f - p) * 8f
                    } else {
                        0f
                    }
                    translationX = shake
                    translationY = -shake * 0.7f
                }
                .padding(horizontal = w * 0.068f, vertical = tall * 0.035f),
            verticalArrangement = Arrangement.Center
        ) {
            VersusCard(
                label = teamAName,
                name = playerA,
                color = FeudColors.team1,
                ink = FeudColors.team1Ink,
                nameColor = FeudColors.team1Ink,
                fromY = -tall * 0.8f,
                fromRotation = -8f,
                nameSize = span * 0.17f,
                clock = clock,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = tall * 0.02f)
            )
            VersusCard(
                label = teamBName,
                name = playerB,
                color = FeudColors.team2,
                ink = FeudColors.team2Ink,
                nameColor = FeudColors.cream,
                fromY = tall * 0.8f,
                fromRotation = 8f,
                nameSize = span * 0.17f,
                clock = clock,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = tall * 0.02f)
            )
        }

        // حلقة الصدمة — بترسم لحالها كل فريم.
        val ringSize = span * 0.5f
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(ringSize * 2.2f)
        ) {
            val ring = shockRing(clock.value, BADGE_AT, 0.6f)
            if (ring.alpha <= 0f) return@Canvas
            val stroke = with(density) { (ring.width * (span.value / 385f)).dp.toPx() }
            drawCircle(
                color = FeudColors.gold.copy(alpha = ring.alpha),
                radius = with(density) { ringSize.toPx() } / 2f * ring.scale,
                style = Stroke(width = stroke.coerceAtLeast(1f))
            )
        }

        // شارة «ضد».
        val badge = span * 0.24f
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(badge)
                .graphicsLayer {
                    val pop = thump(clock.value, BADGE_AT, 0.36f)
                    scaleX = pop
                    scaleY = pop
                }
                .clip(CircleShape)
                .background(FeudColors.gold)
                .padding(badge * 0.076f)
                .clip(CircleShape)
                .background(FeudColors.ink),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "ضد",
                color = FeudColors.cream,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = with(density) { (badge * 0.37f).toSp() }
                )
            )
        }
    }
}

/** كرت لاعب بالمواجهة — بينقذف لمكانه بضربة مطاطية. */
@Composable
private fun VersusCard(
    label: String,
    name: String,
    color: Color,
    ink: Color,
    nameColor: Color,
    fromY: Dp,
    fromRotation: Float,
    nameSize: Dp,
    clock: State<Float>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val fromYPx = with(density) { fromY.toPx() }

    Box(
        modifier = modifier
            .graphicsLayer {
                val time = clock.value
                val away = keys(
                    time, CARD_AT, CARD_TIME,
                    listOf(0f to 1f, 0.54f to 0f, 1f to 0f)
                )
                translationY = fromYPx * away
                rotationZ = fromRotation * away
                scaleX = keys(
                    time, CARD_AT, CARD_TIME,
                    listOf(0f to 0.7f, 0.54f to 0.86f, 0.7f to 1.08f, 0.86f to 0.97f, 1f to 1f)
                )
                scaleY = keys(
                    time, CARD_AT, CARD_TIME,
                    listOf(0f to 0.7f, 0.54f to 1.14f, 0.7f to 0.94f, 0.86f to 1.04f, 1f to 1f)
                )
                alpha = appear(time, CARD_AT, 0.12f)
            }
            .blockSkin(color)
            .clip(RoundedCornerShape(FeudShape.block)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                color = ink.copy(alpha = 0.75f),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                name,
                color = nameColor,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = with(density) { nameSize.toSp() },
                    lineHeight = with(density) { (nameSize * 1.1f).toSp() }
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
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
            .padding(horizontal = 12.dp)
            .blockSkin(FeudColors.gold),
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
