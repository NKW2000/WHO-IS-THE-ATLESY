package com.feudparty.app.ui

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
import com.feudparty.app.ui.components.CategoryRoundBlocks
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

    Box {
        StageBackground(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // فوق: الوقت والأخطاء عاليمين، السؤال بالنص، والتصنيف عالشمال.
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
                            category = state.currentQuestion?.category,
                            question = state.currentQuestion?.text ?: "—",
                            modifier = Modifier.widthIn(max = 560.dp)
                        )
                    }

                    // التصنيف والجولة عالشمال — بلوكين جنب بعض بنفس القياس.
                    CategoryRoundBlocks(
                        category = state.currentQuestion?.category,
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

                // تحت: مين عم يجاوب وكم خطأ عليه، وزر الغلط.
                // تحت: زر الغلط بالنص، والدور عالجنب، والجولة الجاية عالطرف
                // التاني — الأخطاء فوق عالشمال مع الوقت.
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
                    // زاوية تحت: بدّل السؤال قبل ما تبلّش الجولة، وبنفس
                    // المكان زر الجولة الجاية بعد ما تخلص.
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
        StrikeFlash(strikes = state.strikes)
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
