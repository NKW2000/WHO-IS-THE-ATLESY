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
import com.feudparty.app.ui.components.flatShadow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.AnswerBoardGrid
import com.feudparty.app.ui.components.RoundBlock
import com.feudparty.app.ui.components.Countdown
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.QuestionCard
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.StrikeFlash
import com.feudparty.app.ui.components.StrikeRow
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.theme.FeudColors
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
/** عرض الزاوية اللي فيها الوقت — ونفسه بالطرف التاني حتى يتوسّط السؤال. */
private val SIDE_SLOT = 210.dp

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
            contentPadding = if (portrait) {
                PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            } else {
                PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (portrait) {
                    PortraitBoard(
                        state = state,
                        canJudge = canJudge,
                        revealedAll = revealedAll,
                        seconds = seconds,
                        onCorrect = onCorrect,
                        onWrong = onWrong,
                        onNextRound = onNextRound,
                        onChangeQuestion = onChangeQuestion
                    )
                } else {
                    // أفقي: الوقت والأخطاء عاليمين، السؤال بالنص، والتصنيف عالشمال.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.width(SIDE_SLOT),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (seconds > 0) {
                                Countdown(seconds = seconds, size = 44.dp)
                            } else {
                                Spacer(Modifier.width(44.dp))
                            }
                            StrikeRow(
                                strikes = state.strikes,
                                total = state.strikesToSteal,
                                size = 30.dp
                            )
                        }

                        // بطاقة السؤال جوّا Box: هي بتستعمل AnimatedVisibility
                        // اللي بتاكل الـ weight وبتاخد كل العرض إذا حطيناه عليها.
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            QuestionCard(
                                round = state.currentQuestionIndex + 1,
                                totalRounds = state.questions.size,
                                question = state.currentQuestion?.text ?: "—",
                                modifier = Modifier.widthIn(max = 560.dp)
                            )
                        }

                        // التصنيف والجولة عالشمال — بلوكين جنب بعض بنفس القياس.
                        RoundBlock(
                            round = state.currentQuestionIndex + 1,
                            totalRounds = state.questions.size,
                            modifier = Modifier.width(SIDE_SLOT)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    AnswerBoardGrid(
                        answers = state.currentQuestion?.answers.orEmpty(),
                        modifier = Modifier.weight(1f),
                        revealHiddenText = true,
                        enabledSlots = canJudge,
                        onSlotClick = onCorrect
                    )

                    Spacer(Modifier.height(8.dp))

                    // تحت: زر الغلط بالنص، والدور عالجنب، وتبديل السؤال أو
                    // الجولة الجاية عالطرف التاني.
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TurnChip(
                            state = state,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .width(300.dp)
                        )
                        SecondaryButton(
                            text = "غلط ✕",
                            onClick = onWrong,
                            enabled = canJudge,
                            accent = FeudColors.strike,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width(200.dp)
                        )
                        if (state.canChangeQuestion()) {
                            if (state.faceOffFailed) {
                                // الاتنين غلطوا: السؤال محروق، فالزر بيصير أساسي.
                                PrimaryButton(
                                    text = "الاتنين غلطوا — بدّل السؤال",
                                    onClick = onChangeQuestion,
                                    color = FeudColors.gold,
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .width(300.dp)
                                )
                            } else {
                                SecondaryButton(
                                    text = "بدّل السؤال",
                                    onClick = onChangeQuestion,
                                    accent = FeudColors.teal,
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .width(200.dp)
                                )
                            }
                        }
                        if (state.phase == RoundPhase.ROUND_END) {
                            PrimaryButton(
                                text = if (revealedAll) state.nextButtonLabel() else "اكشف الباقي",
                                onClick = onNextRound,
                                enabled = revealedAll,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .width(240.dp)
                            )
                        }
                    }
                }
            }
        }
        StrikeFlash(strikes = state.strikes)
    }
}

/**
 * لوح المضيف بالوضع الطولي — نفس كرت التصميم: الوقت بالزاوية والأخطاء
 * بالطرف التاني، السؤال بلوح كريمي، والأجوبة سطر ورا سطر بكل عرض
 * الشاشة، وتحت زر الغلط.
 */
@Composable
private fun ColumnScope.PortraitBoard(
    state: GameState,
    canJudge: Boolean,
    revealedAll: Boolean,
    seconds: Int,
    onCorrect: (Int) -> Unit,
    onWrong: () -> Unit,
    onNextRound: () -> Unit,
    onChangeQuestion: () -> Unit
) {
    PortraitBoardHeader(
        seconds = seconds,
        strikes = state.strikes,
        total = state.strikesToSteal
    )

    Spacer(Modifier.height(10.dp))

    // السؤال بلوح كريمي بظل مسطّح.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .flatShadow(6.dp, FeudColors.creamShadow, 14.dp)
            .background(FeudColors.cream, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            state.currentQuestion?.text ?: "—",
            color = FeudColors.ink,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    }

    Spacer(Modifier.height(10.dp))

    val answers = state.currentQuestion?.answers.orEmpty()
    val slots = maxOf(answers.size + 1, 8)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(slots) { index ->
            PortraitAnswerRow(
                position = index + 1,
                answer = answers.getOrNull(index),
                enabled = canJudge,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onClick = { onCorrect(index) }
            )
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
                shadow = FeudColors.limeShadow,
                enabled = revealedAll,
                modifier = Modifier.weight(1f),
                onClick = onNextRound
            )
        } else {
            FlatButton(
                text = "غلط ✕",
                color = FeudColors.pink,
                textColor = Color.White,
                shadow = FeudColors.strikeShadow,
                enabled = canJudge,
                modifier = Modifier.weight(1f),
                onClick = onWrong
            )
            if (state.canChangeQuestion()) {
                FlatButton(
                    text = if (state.faceOffFailed) "بدّل السؤال" else "بدّل",
                    color = if (state.faceOffFailed) FeudColors.gold else FeudColors.teal,
                    textColor = FeudColors.ink,
                    shadow = FeudColors.ink,
                    modifier = Modifier.weight(if (state.faceOffFailed) 1f else 0.5f),
                    onClick = onChangeQuestion
                )
            }
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(FeudColors.panelDark)
                .border(3.dp, FeudColors.stageAlt, RoundedCornerShape(14.dp))
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

        Spacer(Modifier.weight(1f))

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

/** سطر جواب بالوضع الطولي: رقم، نص، نقاط — بظل مسطّح زي التصميم. */
@Composable
fun PortraitAnswerRow(
    position: Int,
    answer: Answer?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    // اللاعب ما بيشوف نص الجواب ولا نقاطه قبل ما يكشفه المضيف.
    revealHiddenText: Boolean = true,
    onClick: () -> Unit = {}
) {
    val revealed = answer?.revealed == true
    val shape = RoundedCornerShape(12.dp)

    val base = when {
        answer == null -> Modifier
            .background(FeudColors.panelDark, shape)
            .border(3.dp, FeudColors.stageAlt, shape)

        revealed -> Modifier
            .flatShadow(4.dp, FeudColors.team1Shadow, 12.dp)
            .background(FeudColors.team1, shape)

        else -> Modifier
            .flatShadow(4.dp, FeudColors.creamShadow, 12.dp)
            .background(FeudColors.cream, shape)
            .then(
                if (revealHiddenText) Modifier.border(3.dp, FeudColors.gold, shape) else Modifier
            )
    }

    Row(
        modifier = modifier
            .clip(shape)
            .then(base)
            .then(
                if (answer != null && !revealed && enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(
                    if (answer == null) FeudColors.stageAlt else FeudColors.gold,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                position.ar(),
                color = if (answer == null) FeudColors.textFaint else FeudColors.ink,
                style = MaterialTheme.typography.labelLarge
            )
        }
        if (answer != null) {
            val showText = revealed || revealHiddenText
            Text(
                if (showText) answer.text else "؟ ؟ ؟",
                color = when {
                    revealed -> FeudColors.team1Ink
                    showText -> FeudColors.ink
                    else -> FeudColors.ink.copy(alpha = 0.35f)
                },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (showText) {
                Text(
                    answer.points.ar(),
                    color = if (revealed) FeudColors.team1Ink else FeudColors.ink,
                    style = MaterialTheme.typography.titleMedium
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
    shadow: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .flatShadow(6.dp, shadow, 14.dp)
            .clip(shape)
            .background(if (enabled) color else color.copy(alpha = 0.45f))
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
