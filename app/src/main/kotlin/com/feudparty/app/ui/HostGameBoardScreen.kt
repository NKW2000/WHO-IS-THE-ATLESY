package com.feudparty.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.feudparty.app.ui.components.blockSkin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.AnswerBoardGrid
import com.feudparty.app.ui.components.RoundBlock
import com.feudparty.app.ui.components.Countdown
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.StrikeFlash
import com.feudparty.app.ui.components.StrikeRow
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.components.keys
import com.feudparty.app.ui.components.rememberRevealDelays
import com.feudparty.app.ui.components.rememberShowClock
import com.feudparty.app.ui.components.thump
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudShape
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.Answer
import com.feudparty.core.game.BuzzState
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Player
import com.feudparty.core.game.Question
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState
import com.feudparty.core.game.buzzedTeam

/**
 * لوحة المضيف (أفقية) — بس هون بينحكم صح/غلط، وبس من هون بتتغير الحالة.
 * الكشف بيصير بالضغط على خانة الجواب نفسها.
 *
 * الترتيب: الوقت عالجنب، السؤال بنص الشاشة تماماً، الأجوبة شبكة ٣×٣
 * (تسع خانات دايماً)، وتحت مين دوره وكم خطأ عليه مع زر الغلط. ما في زر
 * «صح»: المضيف بيدوس على خانة الجواب نفسها فبتنقلب خضرا. النقاط ما
 * بتبيّن هون — بتبيّن بشاشة النتيجة بين الجولات.
 */
/** عدد خانات اللوح — أكتر عدد أجوبة بالسؤال. */
private const val BOARD_SLOTS = 8

/** تبديل السؤال مسموح قبل ما تبلّش الجولة فعلياً — يعني بالمواجهة وبدون كشف. */
private fun GameState.canChangeQuestion(): Boolean =
    (phase == RoundPhase.FACE_OFF || phase == RoundPhase.FACE_OFF_SECOND) &&
        currentQuestion?.answers?.none { it.revealed } ?: false

@Composable
fun HostGameBoardScreen(
    state: GameState,
    onCorrect: (Int) -> Unit,
    onWrong: () -> Unit,
    onNextRound: () -> Unit,
    onChangeQuestion: () -> Unit = {}
) {
    val canJudge = state.canJudge()
    val revealedAll = state.boardFullyRevealed()
    val seconds = maxOf(state.answerSecondsLeft, state.choiceSecondsLeft)
    val portrait = isPortrait()

    Box {
        StageBackground(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // نفس التصميم بالوضعين: الفرق الوحيد إنه بالعرضي الأجوبة
                // بتتوزّع على عمودين لأن العرض بيسمح.
                DesignBoard(
                    state = state,
                    canJudge = canJudge,
                    revealedAll = revealedAll,
                    seconds = seconds,
                    columns = if (portrait) 1 else 2,
                    onCorrect = onCorrect,
                    onWrong = onWrong,
                    onNextRound = onNextRound,
                    onChangeQuestion = onChangeQuestion
                )
            }
        }
        StrikeFlash(strikes = state.strikes)
    }
}

/**
 * لوح المضيف — نفس كرت التصميم بالوضعين: الوقت بالزاوية والأخطاء بالطرف
 * التاني، السؤال بلوح كريمي، والأجوبة سطر ورا سطر (عمود بالطولي وعمودين
 * بالعرضي)، وتحت زر الغلط اللي بينقلب «بدّل السؤال» إذا الاتنين غلطوا.
 */
@Composable
private fun ColumnScope.DesignBoard(
    state: GameState,
    canJudge: Boolean,
    revealedAll: Boolean,
    seconds: Int,
    columns: Int,
    onCorrect: (Int) -> Unit,
    onWrong: () -> Unit,
    onNextRound: () -> Unit,
    onChangeQuestion: () -> Unit
) {
    val questionCard: @Composable (Modifier) -> Unit = { cardModifier ->
        Box(
            modifier = cardModifier
                .blockSkin(FeudColors.cream)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                state.currentQuestion?.text ?: "—",
                color = FeudColors.ink,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (columns > 1) {
        // بالعرضي السؤال بينحط بين الوقت والأخطاء — بيوفّر سطر كامل
        // للأجوبة.
        PortraitBoardHeader(
            seconds = seconds,
            strikes = state.strikes,
            total = state.strikesToSteal,
            middle = { questionCard(Modifier.fillMaxWidth()) }
        )
        Spacer(Modifier.height(10.dp))
    } else {
        PortraitBoardHeader(
            seconds = seconds,
            strikes = state.strikes,
            total = state.strikesToSteal
        )
        Spacer(Modifier.height(10.dp))
        questionCard(Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
    }

    // ثمان خانات دايماً — أكتر عدد أجوبة بالسؤال ثمانية.
    val answers = state.currentQuestion?.answers.orEmpty()
    val slots = maxOf(answers.size, BOARD_SLOTS)
    val perColumn = (slots + columns - 1) / columns
    // «اكشف الباقي» بيكشف كذا خانة مرة وحدة — بتتقلب وحدة ورا التانية.
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
                        enabled = canJudge,
                        revealDelay = revealDelays[index] ?: 0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        onClick = { onCorrect(index) }
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (state.phase == RoundPhase.ROUND_END) {
            FlatButton(
                text = if (revealedAll) state.nextButtonLabel() else "اكشف الباقي",
                color = FeudColors.lime,
                textColor = FeudColors.ink,
                enabled = revealedAll,
                modifier = Modifier.weight(1f),
                onClick = onNextRound
            )
        } else if (state.faceOffFailed && state.canChangeQuestion()) {
            // الاتنين غلطوا: نفس الزر بمكانه بينقلب «بدّل السؤال».
            FlatButton(
                text = "بدّل السؤال ⟳",
                color = FeudColors.gold,
                textColor = FeudColors.ink,
                modifier = Modifier.weight(1f),
                onClick = onChangeQuestion
            )
        } else {
            FlatButton(
                text = "غلط ✕",
                color = FeudColors.pink,
                textColor = Color.White,
                enabled = canJudge,
                modifier = Modifier.weight(1f),
                onClick = onWrong
            )
        }
    }
}

/**
 * سطر الوقت والأخطاء بالوضع الطولي: الوقت عاليمين والأخطاء عالشمال —
 * نفس الشريط عند المضيف وعند اللاعب.
 */
@Composable
fun PortraitBoardHeader(
    seconds: Int,
    strikes: Int,
    total: Int,
    modifier: Modifier = Modifier,
    // بالعرضي بينحط السؤال بين الوقت والأخطاء.
    middle: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(FeudShape.block))
                .background(FeudColors.panelDark)
                .border(3.dp, FeudColors.stageAlt, RoundedCornerShape(FeudShape.block))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                if (seconds > 0) seconds.ar() else "—",
                color = FeudColors.gold,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                "ثانية",
                color = FeudColors.gold.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelMedium
            )
        }

        if (middle != null) {
            Box(modifier = Modifier.weight(1f)) { middle() }
        } else {
            Spacer(Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            repeat(total) { index ->
                val lit = index < strikes
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .drawBehind {
                            if (lit) {
                                drawCircle(
                                    color = FeudColors.strikeShadow,
                                    radius = size.minDimension / 2f,
                                    center = Offset(
                                        size.width / 2f,
                                        size.height / 2f + 3.dp.toPx()
                                    )
                                )
                            }
                        }
                        .background(
                            if (lit) FeudColors.pink else FeudColors.stageAlt,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✕",
                        color = if (lit) Color.White else FeudColors.outlineSoft,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

/** زمن انقلاب الخانة لما تنكشف. */
private const val FLIP_TIME = 0.5f

/** الهزّة لما يحطّ الوجه الأخضر، ونطّة النقاط بعدها بشوي. */
private const val LAND_AT = FLIP_TIME * 0.35f
private const val POINTS_AT = FLIP_TIME * 0.45f

/** وقت «خلصت الحركة» للخانة اللي بتبيّن مكشوفة بدون ما تتحرّك. */
private const val SETTLED = 1e6f

/**
 * سطر جواب: رقم، نص، نقاط — بظل مسطّح زي التصميم. نفس السطر عند المضيف
 * وعند اللاعب، فالكشف بيتحرّك عند الاتنين متل بعض: الخانة بتنقلب على
 * محورها الأفقي من الكريمي للأخضر، بتهتز لما تحطّ، والنقاط بتنط بعدها.
 * [revealDelay] بيأخّر الانقلاب لما تنكشف كذا خانة بنفس اللحظة.
 *
 * الوجهين مرسومين فوق بعض والوقت بينقرأ جوّا `graphicsLayer` بس — كل
 * فريم بيعيد الرسم بدون ما يعيد تركيب السطر.
 */
@Composable
fun PortraitAnswerRow(
    position: Int,
    answer: Answer?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    // اللاعب ما بيشوف نص الجواب ولا نقاطه قبل ما يكشفه المضيف.
    revealHiddenText: Boolean = true,
    revealDelay: Float = 0f,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(FeudShape.block)

    if (answer == null) {
        SlotFace(
            position = position,
            text = null,
            textColor = FeudColors.textFaint,
            points = null,
            pointsColor = FeudColors.textFaint,
            numberColor = FeudColors.stageAlt,
            numberInk = FeudColors.textFaint,
            modifier = modifier
                .clip(shape)
                .background(FeudColors.panelDark, shape)
                .border(3.dp, FeudColors.stageAlt, shape)
        )
        return
    }

    val revealed = answer.revealed
    // بنحرّك بس الكشف اللي صار قدّامنا: خانة أول ما شفناها كانت مكشوفة
    // (لاعب انضم بنص الجولة) بتبيّن خضرا فوراً.
    val gate = remember { RevealGate(seenHidden = !revealed) }
    if (!revealed) gate.seenHidden = true
    val animate = revealed && gate.seenHidden
    val clock = rememberShowClock(
        key = animate,
        cap = if (animate) revealDelay + FLIP_TIME + 0.6f else 0f
    )
    val now: () -> Float = {
        when {
            animate -> clock.value
            revealed -> SETTLED
            else -> 0f
        }
    }
    val showText = revealed || revealHiddenText

    Box(modifier = modifier) {
        // الوجه الأخضر — بيجي من الحرف لمكانه بعد ما يختفي الكريمي.
        SlotFace(
            position = position,
            text = answer.text,
            textColor = FeudColors.team1Ink,
            points = answer.points.ar(),
            pointsColor = FeudColors.team1Ink,
            numberColor = FeudColors.gold,
            numberInk = FeudColors.ink,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    val t = now()
                    val angle = flipAngle(t, revealDelay)
                    alpha = if (angle < 90f) 0f else 1f
                    rotationX = angle - 180f
                    cameraDistance = 12f * density
                    val land = keys(
                        t, revealDelay + LAND_AT, 0.4f,
                        listOf(0f to 1f, 0.45f to 1.06f, 0.75f to 0.98f, 1f to 1f)
                    )
                    scaleX = land
                    scaleY = land
                }
                .clip(shape)
                .blockSkin(FeudColors.team1, border = 3.dp, shadow = 4.dp),
            pointsModifier = Modifier.graphicsLayer {
                val pop = thump(now(), revealDelay + POINTS_AT, 0.4f)
                scaleX = pop
                scaleY = pop
            }
        )

        // الوجه الكريمي — فوق، لأنه هو اللي بينضغط عند المضيف.
        SlotFace(
            position = position,
            text = if (showText) answer.text else "؟ ؟ ؟",
            textColor = if (showText) FeudColors.ink else FeudColors.ink.copy(alpha = 0.35f),
            points = if (showText) answer.points.ar() else null,
            pointsColor = FeudColors.ink,
            numberColor = FeudColors.gold,
            numberInk = FeudColors.ink,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    val angle = flipAngle(now(), revealDelay)
                    alpha = if (angle < 90f) 1f else 0f
                    rotationX = angle
                    cameraDistance = 12f * density
                }
                .clip(shape)
                .blockSkin(FeudColors.cream, border = 3.dp, shadow = 4.dp)
                .then(
                    if (!revealed && enabled) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    }
                )
        )
    }
}

/** بيتذكّر إذا شفنا الخانة مخفية قبل — بس هيك الكشف بيتحرّك. */
private class RevealGate(var seenHidden: Boolean)

/**
 * زاوية الانقلاب من ٠ لـ ١٨٠: النص الأول بيلف الوجه الكريمي لحد ما يصير
 * عالحرف، والتاني بيجيب الأخضر من الحرف لمكانه — بمنحنى `bang` نفسه،
 * فالكريمي بيختفي بسرعة والأخضر بيهدى وهو نازل.
 */
private fun flipAngle(t: Float, delay: Float): Float =
    180f * keys(t, delay, FLIP_TIME, listOf(0f to 0f, 1f to 1f))

/** وجه واحد من الخانة: رقم، نص، ونقاط. [text] = null يعني خانة فاضية. */
@Composable
private fun SlotFace(
    position: Int,
    text: String?,
    textColor: Color,
    points: String?,
    pointsColor: Color,
    numberColor: Color,
    numberInk: Color,
    modifier: Modifier,
    pointsModifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(numberColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                position.ar(),
                color = numberInk,
                style = MaterialTheme.typography.labelLarge
            )
        }
        if (text != null) {
            Text(
                text,
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (points != null) {
                Text(
                    points,
                    color = pointsColor,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = pointsModifier
                )
            }
        } else {
            Spacer(Modifier.weight(1f))
        }
    }
}

/** زر مسطّح بظل تحته — شكل أزرار التصميم بالوضع الطولي. */
@Composable
private fun FlatButton(
    text: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(FeudShape.block)
    Box(
        modifier = modifier
            .blockSkin(if (enabled) color else color.copy(alpha = 0.45f))
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) textColor else textColor.copy(alpha = 0.6f),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )
    }
}

/** مين عم يجاوب هلق — اسمه ورقمه ولون فريقه. */
@Composable
private fun TurnChip(state: GameState, modifier: Modifier = Modifier) {
    val player = state.player(state.turnPlayerId) ?: state.player(state.buzzedPlayerId)
    val team = player?.teamId ?: state.activeTeam
    val color = team?.color() ?: FeudColors.textMuted

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (player != null) {
            SeatBadge(seat = player.seat, dim = !player.connected, size = 30.dp)
            Spacer(Modifier.width(10.dp))
        }
        Column {
            Text(
                "دور",
                color = FeudColors.goldDim,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                player?.name ?: state.teams[team]?.name ?: "—",
                color = color,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (player != null) {
            Spacer(Modifier.width(10.dp))
            Text(
                state.teams[player.teamId]?.name.orEmpty(),
                color = FeudColors.textMuted,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** المضيف بيقدر يحكم بس لما يكون في لاعب مستنّي حكم. */
private fun GameState.canJudge(): Boolean = when (phase) {
    // إذا ما ضل حدا يقدر يضغط (كلهم انقطعوا) المضيف بيكمّل بإيده.
    RoundPhase.FACE_OFF -> buzzedTeam() != null || armedPlayerIds().isEmpty()
    RoundPhase.FACE_OFF_SECOND, RoundPhase.PLAY, RoundPhase.STEAL -> true
    // بعد نهاية الجولة الخانات بتضل تنضغط حتى يكشف الباقي وحدة وحدة.
    RoundPhase.ROUND_END -> true
    else -> false
}

/** ما بينتقل للجولة الجاية إلا لما يكشف كل اللوح. */
private fun GameState.boardFullyRevealed(): Boolean =
    currentQuestion?.answers?.all { it.revealed } ?: true

private fun GameState.nextButtonLabel(): String =
    if (isLastRound) "إنهاء اللعبة" else "الجولة الجاية"

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun HostGameBoardScreenPreview() {
    FeudPartyTheme {
        HostGameBoardScreen(
            state = GameState(
                questions = listOf(
                    Question(
                        "q1",
                        "اذكر شي بيعمله الناس أول ما يصحوا",
                        listOf(
                            Answer("يشيّكوا الموبايل", 40, revealed = true),
                            Answer("يشربوا قهوة", 30),
                            Answer("يغسلوا وجّهم", 20),
                            Answer("يصلّوا", 10)
                        ),
                        "عام"
                    )
                ),
                players = listOf(
                    Player("a1", "سامر", TeamId.TEAM_1, seat = 1),
                    Player("a2", "هناء", TeamId.TEAM_1, seat = 2),
                    Player("a3", "زيد", TeamId.TEAM_1, seat = 3),
                    Player("b1", "ليلى", TeamId.TEAM_2, seat = 1)
                ),
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "الفريق الأخضر", 140, connected = true),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الفريق الأزرق", 95, connected = true)
                ),
                phase = RoundPhase.PLAY,
                controllingTeam = TeamId.TEAM_1,
                turnPlayerId = "a2",
                wrongPlayers = setOf("a3"),
                correctPlayers = setOf("a1"),
                buzzState = BuzzState.CLOSED,
                pot = 40,
                strikes = 2
            ),
            onCorrect = {},
            onWrong = {},
            onNextRound = {}
        )
    }
}
