package com.feudparty.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.AnswerBoardGrid
import com.feudparty.app.ui.components.Countdown
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.QuestionCard
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.StatusBanner
import com.feudparty.app.ui.components.StrikeFlash
import com.feudparty.app.ui.components.StrikeRow
import com.feudparty.app.ui.components.accentFor
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
 * الترتيب: الوقت عالجنب، السؤال بالنص فوق، الأجوبة شبكة تلاتة بالسطر،
 * وتحت مين دوره وكم خطأ عليه مع زر الغلط. النقاط ما بتبيّن هون — بتبيّن
 * بشاشة النتيجة بين الجولات.
 */
@Composable
fun HostGameBoardScreen(
    state: GameState,
    onCorrect: (Int) -> Unit,
    onWrong: () -> Unit,
    onNextRound: () -> Unit
) {
    val canJudge = state.canJudge()
    val accent = accentFor(state.activeTeam)
    val revealedAll = state.boardFullyRevealed()
    val seconds = maxOf(state.answerSecondsLeft, state.choiceSecondsLeft)

    Box {
        StageBackground(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // فوق: الوقت عاليمين، السؤال بالنص، والحالة عالطرف التاني.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.width(96.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (seconds > 0) Countdown(seconds = seconds)
                    }
                    // بطاقة السؤال جوّا Box: هي بتستعمل AnimatedVisibility
                    // اللي بتاكل الـ weight وبتاخد كل العرض إذا حطيناه عليها.
                    Box(modifier = Modifier.weight(1f)) {
                        QuestionCard(
                            round = state.currentQuestionIndex + 1,
                            totalRounds = state.questions.size,
                            category = state.currentQuestion?.category,
                            question = state.currentQuestion?.text ?: "—"
                        )
                    }
                    Box(
                        modifier = Modifier.width(200.dp).padding(start = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        StatusBanner(text = hostStatusText(state), accent = accent)
                    }
                }

                Spacer(Modifier.height(10.dp))

                AnswerBoardGrid(
                    answers = state.currentQuestion?.answers.orEmpty(),
                    modifier = Modifier.weight(1f),
                    revealHiddenText = true,
                    enabledSlots = canJudge,
                    onSlotClick = onCorrect
                )

                Spacer(Modifier.height(10.dp))

                // تحت: مين عم يجاوب وكم خطأ عليه، وزر الغلط.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TurnChip(state = state, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    StrikeRow(
                        strikes = state.strikes,
                        total = state.strikesToSteal,
                        size = 34.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    SecondaryButton(
                        text = "غلط ✕",
                        onClick = onWrong,
                        enabled = canJudge,
                        accent = FeudColors.strike,
                        modifier = Modifier.width(180.dp)
                    )
                    if (state.phase == RoundPhase.ROUND_END) {
                        Spacer(Modifier.width(12.dp))
                        PrimaryButton(
                            text = if (revealedAll) state.nextButtonLabel() else "اكشف الباقي",
                            onClick = onNextRound,
                            enabled = revealedAll,
                            modifier = Modifier.width(240.dp)
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
