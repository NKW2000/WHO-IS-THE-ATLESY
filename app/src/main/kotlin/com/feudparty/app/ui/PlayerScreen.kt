package com.feudparty.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.AnswerBoardGrid
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.RoundBlock
import com.feudparty.app.ui.components.Countdown
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.rememberRevealDelays
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StrikeFlash
import com.feudparty.app.ui.components.StrikeRow
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.components.inkColor
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudShape
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.app.viewmodel.PlayerViewModel.ConnectionStatus
import com.feudparty.core.game.Answer
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Player
import com.feudparty.core.game.PlayerMark
import com.feudparty.core.game.Question
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState
import com.feudparty.core.game.other
import kotlin.math.abs
import kotlinx.coroutines.delay

/** ألوان شاشة «العب / تمرير» زي ما هي بملف التصميم. */
private val PassPlayBase = FeudColors.canvas
private val PassColor = FeudColors.pink
private val PlayColor = FeudColors.team1
private val PlayInk = FeudColors.team1Ink
private val TimerColor = FeudColors.gold
private val TRACK_HEIGHT = 16.dp
private const val DOOR_MILLIS = 640
private val DoorEasing = CubicBezierEasing(0.2f, 0.85f, 0.25f, 1f)

/** قطر زر البزّ. */
private val BUZZER_SIZE = 190.dp

/**
 * جهاز اللاعب. شاشتين بس:
 *
 * 1. **الزر** — لما يكون دورك بالمواجهة، الشاشة كلها زر واحد بيومض. ما في
 *    ولا عنصر تاني، فما بتغلط بالضغط حتى لو ما بتتطلع عالجهاز.
 * 2. **اللوح** — بعد ما تضغط (أو لما يجي دورك باللعب) بيبيّن السؤال
 *    والخانات الفاضية، وبتنكشف وحدة وحدة مع حكم المضيف.
 *
 * اللون بيضل هو الرسالة: أزرق ضغطت، أخضر صح، أحمر غلط.
 */
@Composable
fun PlayerScreen(
    state: GameState?,
    playerId: String?,
    teamId: TeamId?,
    mark: PlayerMark,
    status: ConnectionStatus,
    onBuzz: () -> Unit,
    onChoose: (Boolean) -> Unit = {},
    onChangeTeam: (TeamId) -> Unit = {}
) {
    val connected = status == ConnectionStatus.CONNECTED

    // قبل ما يبلّش المضيف (وكمان بعد ما يرجّع اللوبي): اللاعب بيشوف رقمه
    // وفريقه وبيقدر يبدّل.
    if (connected && state != null && !state.matchStarted && !state.gameOver) {
        PlayerLobbyScreen(
            state = state,
            playerId = playerId,
            teamId = teamId,
            onChangeTeam = onChangeTeam
        )
        return
    }

    // الفائز بالمواجهة بيقرر من جهازه: نلعب أو نمرّر.
    if (connected &&
        state?.phase == RoundPhase.PLAY_OR_PASS &&
        playerId != null &&
        playerId in state.armedPlayerIds()
    ) {
        PlayOrPassScreen(state = state, teamId = teamId, onChoose = onChoose)
        return
    }

    val faceOffBuzzer = connected &&
        mark == PlayerMark.ARMED &&
        state?.phase == RoundPhase.FACE_OFF

    if (faceOffBuzzer) {
        FullScreenBuzzer(
            onBuzz = onBuzz
        )
    } else {
        Box {
            PlayerBoard(
                state = state,
                playerId = playerId,
                teamId = teamId,
                mark = mark,
                status = status,
                onBuzz = onBuzz
            )
            // نفس حركة الخطأ اللي بتطلع عند المضيف — بتطلع عند الكل،
            // وكمان لما يخلص الوقت بدون جواب.
            StrikeFlash(strikes = state?.strikes ?: 0)
        }
    }
}

/**
 * لوبي اللاعب: اسمه ورقمه وفريقه، ومين معه بالفريق، وزر يبدّل فيه فريقه
 * قبل ما تبلّش اللعبة.
 */
@Composable
private fun PlayerLobbyScreen(
    state: GameState,
    playerId: String?,
    teamId: TeamId?,
    onChangeTeam: (TeamId) -> Unit
) {
    val me = state.player(playerId)
    val other = teamId?.other()

    Box(modifier = Modifier.fillMaxSize().background(FeudColors.stage).padding(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // بالطولي الاسم بسطر والحالة بسطر تحته — ما بينقص الاسم.
            if (isPortrait()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (me != null) {
                        SeatBadge(seat = me.seat, size = 40.dp)
                        Text(
                            me.name,
                            color = FeudColors.cream,
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "بانتظار المضيف يبلّش",
                    color = FeudColors.gold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (me != null) {
                        SeatBadge(seat = me.seat, size = 34.dp)
                        Text(
                            me.name,
                            color = FeudColors.cream,
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "بانتظار المضيف يبلّش",
                        color = FeudColors.gold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // الفرق بأسماء المضيف: دوس على فريق حتى تفوت فيه. بالطولي
            // بيصيروا فوق بعض وأسماء اللاعبين بعمودين — زي التصميم.
            if (isPortrait()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TeamId.entries.forEach { id ->
                        TeamRoster(
                            state = state,
                            teamId = id,
                            mine = id == teamId,
                            onClick = { if (id != teamId) onChangeTeam(id) },
                            twoColumns = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    TeamId.entries.forEachIndexed { index, id ->
                        if (index > 0) Spacer(Modifier.width(12.dp))
                        TeamRoster(
                            state = state,
                            teamId = id,
                            mine = id == teamId,
                            onClick = { if (id != teamId) onChangeTeam(id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                if (other == null) "" else "دوس على الفريق التاني إذا بدك تبدّل",
                color = FeudColors.textMuted,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** أسماء فريق مرتّبة برقم كل لاعب. */
@Composable
private fun TeamRoster(
    state: GameState,
    teamId: TeamId,
    mine: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    twoColumns: Boolean = false
) {
    val team = state.teams[teamId]
    CartoonSurface(
        modifier = modifier,
        // الفريقين بلون النظام بالضبط — «فريقك» والحدّ الذهبي هني اللي
        // بيميّزوا فريقك، مش تخفيف اللون.
        color = teamId.color(),
        corner = FeudShape.block,
        shadow = 7.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(if (twoColumns) 18.dp else 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    team?.name ?: "فريق",
                    color = teamId.inkColor(),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (mine) {
                    Text(
                        "فريقك",
                        color = teamId.inkColor(),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            val roster = state.playersOf(teamId)
            val rows = if (twoColumns) roster.chunked(2) else roster.map { listOf(it) }
            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { player ->
                        Row(
                            modifier = if (twoColumns) Modifier.weight(1f) else Modifier,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SeatBadge(seat = player.seat, size = if (twoColumns) 30.dp else 24.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                player.name,
                                color = teamId.inkColor(),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (twoColumns && row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            if (state.playersOf(teamId).isEmpty()) {
                Text(
                    "لسا ما فات حدا",
                    color = FeudColors.textMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/** قرار الفائز بالمواجهة — زرين كبار، بدون أي إشي تاني بالشاشة. */
@Composable
private fun PlayOrPassScreen(
    state: GameState,
    teamId: TeamId?,
    onChoose: (Boolean) -> Unit
) {
    val other = state.teams[teamId?.other()]?.name ?: "الفريق التاني"
    val total = state.choiceLimitSeconds.coerceAtLeast(1)

    // ثلاث مراحل زي التصميم: برّا الكادر، داخل ومستقر، ومفتوح بعد الاختيار.
    var opened by remember { mutableStateOf(false) }
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(120)
        entered = true
    }
    val away = entered && !opened

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(PassPlayBase)
    ) {
        val width = maxWidth
        val bandHeight = (maxHeight - TRACK_HEIGHT) / 2
        // بالطولي الشريط بيصير عالي كتير — منحدّ الخط بعرض الشاشة.
        val labelSize = minOf(bandHeight * 0.3f, maxWidth * 0.2f)

        val slide = tween<Dp>(durationMillis = DOOR_MILLIS, easing = DoorEasing)
        val slideLate = tween<Dp>(
            durationMillis = DOOR_MILLIS,
            delayMillis = 110,
            easing = DoorEasing
        )

        // «تمرير» بينزلق من اليمين، و«العب» من الشمال بفارق بسيط.
        val passOffset by animateDpAsState(
            targetValue = if (away) 0.dp else width,
            animationSpec = slide,
            label = "passBand"
        )
        val playOffset by animateDpAsState(
            targetValue = if (away) 0.dp else -width,
            animationSpec = slideLate,
            label = "playBand"
        )

        // اللي تحت البابين: بيبيّن لما ينفتحوا بعد الاختيار.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Pill(
                text = "الجولة ${(state.currentQuestionIndex + 1).ar()}",
                color = FeudColors.stageAlt,
                textColor = FeudColors.textMuted
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "كسبتوا المواجهة!",
                color = FeudColors.cream,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (opened) "اختيارك انحفظ، استنى باقي الفريق." else "تلعبوا اللوح ولا تمرّروه لـ$other؟",
                color = FeudColors.textSoft,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }

        // شريط «تمرير» الأحمر فوق.
        Band(
            label = "مرّر",
            background = PassColor,
            labelColor = FeudColors.cream,
            height = bandHeight,
            fontSize = labelSize,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = passOffset),
            onClick = {
                if (!opened) {
                    opened = true
                    onChoose(false)
                }
            }
        )

        // شريط الوقت ملزوق بالأحمر: بينفضى بسرعة ثابتة من الوقت نفسه، مش من
        // دقّات المضيف — الدقّة اللي بتوصل عالشبكة متأخرة أو بدري ما بتوقّفه
        // ولا بتنطّطه، بس إذا بعد كتير عن وقت المضيف منرجّعه على وقته.
        val progress = remember { Animatable(1f) }
        LaunchedEffect(state.choiceSecondsLeft, total) {
            val hostAt = (state.choiceSecondsLeft.toFloat() / total).coerceIn(0f, 1f)
            val drift = abs(progress.value - hostAt) * total
            // عدّاد جديد (رجع الوقت لفوق) أو فرق أكتر من ثانية ونص: منقفز لوقت المضيف.
            if (hostAt > progress.value + 0.02f || drift > 1.5f) progress.snapTo(hostAt)
            if (state.choiceSecondsLeft <= 0) {
                progress.snapTo(0f)
                return@LaunchedEffect
            }
            // من وين ما كنا لصفر، بسرعة (١ / الوقت الكلي) بالثانية — بدون وقفات.
            val remainingMillis = (progress.value * total * 1000).toInt().coerceAtLeast(1)
            progress.animateTo(0f, tween(durationMillis = remainingMillis, easing = LinearEasing))
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = passOffset, y = bandHeight)
                .fillMaxWidth()
                .height(TRACK_HEIGHT)
                .background(PassPlayBase)
        ) {
            // بينفضى من اليمين للشمال: الباقي ملزوق بالطرف الشمالي.
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(progress.value)
                    .fillMaxHeight()
                    .background(TimerColor)
            )
        }

        // شريط «العب» الأخضر تحت.
        Band(
            label = "العب",
            background = PlayColor,
            labelColor = PlayInk,
            height = bandHeight,
            fontSize = labelSize,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = playOffset),
            onClick = {
                if (!opened) {
                    opened = true
                    onChoose(true)
                }
            }
        )
    }
}

/** شريط ملوّن كامل العرض — مستطيل صافي بدون قصّات، زي التصميم. */
@Composable
private fun Band(
    label: String,
    background: Color,
    labelColor: Color,
    height: Dp,
    fontSize: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val size = with(LocalDensity.current) { fontSize.toSp() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = labelColor,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = size,
                lineHeight = size * 1.1f
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * الشاشة كلها زر. الخلفية ثابتة (بنفسجي المسرح) وبس الزر بينبض وحواليه
 * حلقة بتتوسّع — مشدود للعين بدون وميض أبيض/أسود بيوجعها.
 */
@Composable
private fun FullScreenBuzzer(
    onBuzz: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val transition = rememberInfiniteTransition(label = "buzzer")
    val pulse by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(760, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val halo by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "halo"
    )

    val interaction = remember { MutableInteractionSource() }
    val shadow = 10.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FeudColors.stage)
            .clickable(
                interactionSource = interaction,
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onBuzz()
            },
        contentAlignment = Alignment.Center
    ) {
        // الزر وحلقته: الحلقة متمركزة على الزر نفسه — الظل تحته بالنص
        // وما بيدخل بحساب حجمه.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                // حلقتين بتتوسّعوا وراء بعض — نفس التصميم (١٤٠٠ms وفارق نصّها).
                val buttonSize = if (isPortrait()) shortSide() * 0.70f else BUZZER_SIZE
                val ringSize = buttonSize * 1.27f
                Canvas(modifier = Modifier.size(ringSize)) {
                    listOf(halo, (halo + 0.5f) % 1f).forEach { phase ->
                        drawCircle(
                            color = FeudColors.gold.copy(alpha = (1f - phase) * 0.35f),
                            radius = (size.minDimension / 2f) * (0.62f + phase * 0.38f),
                            style = Stroke(width = 8.dp.toPx())
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .scale(pulse)
                        .drawBehind {
                            // ظل صلب تحت الزر بالضبط، مش على جنب.
                            drawCircle(
                                color = FeudColors.ink,
                                radius = size.minDimension / 2f,
                                center = Offset(size.width / 2f, size.height / 2f + shadow.toPx())
                            )
                        }
                        .background(FeudColors.gold, CircleShape)
                        .border(6.dp, FeudColors.ink, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "اضغط!",
                        color = FeudColors.ink,
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerBoard(
    state: GameState?,
    playerId: String?,
    teamId: TeamId?,
    mark: PlayerMark,
    status: ConnectionStatus,
    onBuzz: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val target = when (mark) {
        PlayerMark.BUZZED -> FeudColors.team2
        PlayerMark.CORRECT -> FeudColors.team1
        PlayerMark.WRONG -> FeudColors.pink
        else -> FeudColors.stage
    }
    val background by animateColorAsState(target, tween(220), label = "boardColor")

    // بمرحلة اللعب دورك بينبّه: أي لمسة بتقول للمضيف إنك عم تجاوب.
    val canSignal = mark == PlayerMark.ARMED
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .clickable(
                enabled = canSignal,
                interactionSource = interaction,
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onBuzz()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val question = state?.currentQuestion
            val seconds = state?.let { maxOf(it.answerSecondsLeft, it.choiceSecondsLeft) } ?: 0

            // نفس لوح المضيف بالضبط — بس بدون السؤال وبدون زر الغلط،
            // وبالعرضي الأجوبة بتتوزّع على عمودين.
            val columns = if (isPortrait()) 1 else 2
            PortraitBoardHeader(
                seconds = seconds,
                strikes = state?.strikes ?: 0,
                total = state?.strikesToSteal ?: 3
            )
            Spacer(Modifier.height(10.dp))

            val answers = question?.answers.orEmpty()
            val slots = maxOf(answers.size, 8)
            val perColumn = (slots + columns - 1) / columns
            val revealDelays = rememberRevealDelays(answers)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(columns) { column ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (row in 0 until perColumn) {
                            val index = column * perColumn + row
                            if (index >= slots) {
                                Spacer(Modifier.weight(1f))
                                continue
                            }
                            PortraitAnswerRow(
                                position = index + 1,
                                answer = answers.getOrNull(index),
                                enabled = false,
                                revealHiddenText = false,
                                revealDelay = revealDelays[index] ?: 0f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            // تحت: بلوك واحد بيقول مين عم يلعب برقمه، وشو المطلوب منّك.
            // نقاط الفرق مش هون — بتبيّن بشاشة النتيجة بين الجولات.
            TurnBlock(
                state = state,
                playerId = playerId,
                teamId = teamId,
                mark = mark,
                status = status
            )
        }
    }
}

/** بلوك الدور: رقم اللاعب اللي عليه الدور واسمه، وتحته سطر الحالة. */
@Composable
private fun TurnBlock(
    state: GameState?,
    playerId: String?,
    teamId: TeamId?,
    mark: PlayerMark,
    status: ConnectionStatus
) {
    val me = state?.player(playerId)
    val current = state?.player(state.turnPlayerId ?: state.buzzedPlayerId) ?: me
    val color = (current?.teamId ?: teamId)?.color() ?: FeudColors.gold
    val ink = (current?.teamId ?: teamId)?.inkColor() ?: FeudColors.ink

    CartoonSurface(
        modifier = Modifier.fillMaxWidth(),
        color = color,
        borderWidth = 3.dp,
        corner = FeudShape.block,
        shadow = 5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (current != null) {
                SeatBadge(seat = current.seat, size = 28.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    if (current.id == playerId) "دورك" else "دور ${current.name}",
                    color = ink,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Spacer(Modifier.width(12.dp))
            }
            Text(
                statusLine(mark, state, teamId, status),
                color = ink,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.End,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun connectionLabel(status: ConnectionStatus): String = when (status) {
    ConnectionStatus.IDLE -> "غير متصل"
    ConnectionStatus.SEARCHING -> "جاري البحث عن المضيف..."
    ConnectionStatus.CONNECTED -> "متصل"
    ConnectionStatus.DISCONNECTED -> "انقطع الاتصال"
}


private fun statusLine(
    mark: PlayerMark,
    state: GameState?,
    teamId: TeamId?,
    status: ConnectionStatus
): String {
    if (status != ConnectionStatus.CONNECTED) return connectionLabel(status)
    if (state == null) return "بانتظار المضيف"
    if (state.gameOver) return "انتهت اللعبة"

    return when (mark) {
        PlayerMark.ARMED -> when (state.phase) {
            RoundPhase.STEAL -> "دوس عالشاشة لتجاوب — عندك جواب واحد بس"
            else -> "دورك — دوس عالشاشة لتجاوب"
        }

        PlayerMark.BUZZED -> "ضغطت! المضيف عم يسمع جوابك"
        PlayerMark.CORRECT -> "صح ✔ — الدور بينتقل لزميلك"
        PlayerMark.WRONG -> "غلط ✘ — استنى لحد ما يخلّص زمايلك"
        PlayerMark.IDLE -> when {
            state.phase == RoundPhase.PLAY_OR_PASS ->
                "${state.teams[state.faceOffWinner]?.name ?: ""} عم يقرر يلعب أو يمرّر"

            state.phase == RoundPhase.ROUND_END && state.roundWinner == teamId -> "الجولة إلنا!"
            state.phase == RoundPhase.ROUND_END -> "انتهت الجولة"
            state.turnPlayerId != null ->
                state.player(state.turnPlayerId)?.name?.let { "الدور على $it" } ?: "استنى دورك"

            state.activeTeam == teamId -> "دور فريقك"
            else -> "استنى دورك"
        }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun PlayerBoardPreview() {
    FeudPartyTheme {
        PlayerScreen(
            state = GameState(
                questions = listOf(
                    Question(
                        "q1",
                        "اذكر شي بيعمله الناس أول ما يصحوا",
                        listOf(
                            Answer("يشيّكوا الموبايل", 40, revealed = true),
                            Answer("", 30),
                            Answer("", 20),
                            Answer("", 10)
                        ),
                        "عام"
                    )
                ),
                players = listOf(
                    Player("p1", "سامر", TeamId.TEAM_1, seat = 1),
                    Player("p2", "ليلى", TeamId.TEAM_2, seat = 2)
                ),
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "الأحمر", score = 120),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الأزرق", score = 80)
                ),
                phase = RoundPhase.PLAY,
                controllingTeam = TeamId.TEAM_1,
                turnPlayerId = "p1",
                strikes = 2
            ),
            playerId = "p1",
            teamId = TeamId.TEAM_1,
            mark = PlayerMark.ARMED,
            status = ConnectionStatus.CONNECTED,
            onBuzz = {}
        )
    }
}
