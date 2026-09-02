package com.feudparty.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.AnswerBoard
import com.feudparty.app.ui.components.AwardBanner
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.QuestionCard
import com.feudparty.app.ui.components.ScoreHeader
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.StatusBanner
import com.feudparty.app.ui.components.StrikeFlash
import com.feudparty.app.ui.components.StrikeRow
import com.feudparty.app.ui.components.accentFor
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.Answer
import com.feudparty.core.game.BuzzState
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState
import com.feudparty.core.game.buzzedTeam

/**
 * لوحة المضيف — بس هون بينحكم صح/غلط، وبس من هون بتتغير الحالة.
 * الكشف بيصير بالضغط على خانة الجواب نفسها.
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

    Box {
        StageBackground(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                ScoreHeader(state)
                Spacer(Modifier.height(12.dp))

                QuestionCard(
                    round = state.currentQuestionIndex + 1,
                    totalRounds = state.questions.size,
                    category = state.currentQuestion?.category,
                    question = state.currentQuestion?.text ?: "—"
                )
                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBanner(
                        text = hostStatusText(state),
                        accent = accent,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.phase == RoundPhase.PLAY || state.phase == RoundPhase.STEAL) {
                        Spacer(Modifier.width(10.dp))
                        StrikeRow(strikes = state.strikes)
                    }
                }
                Spacer(Modifier.height(10.dp))

                AwardBanner(state)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    AnswerBoard(
                        answers = state.currentQuestion?.answers.orEmpty(),
                        revealHiddenText = true,
                        enabledSlots = canJudge,
                        onSlotClick = onCorrect
                    )
                    Spacer(Modifier.height(8.dp))
                    if (canJudge) {
                        Text(
                            "اضغط على خانة الجواب لكشفها",
                            color = FeudColors.textMuted,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryButton(
                        text = "غلط ✕",
                        onClick = onWrong,
                        enabled = canJudge,
                        accent = FeudColors.strike,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = state.nextButtonLabel(),
                        onClick = onNextRound,
                        enabled = state.phase == RoundPhase.ROUND_END,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (state.phase == RoundPhase.FACE_OFF && state.buzzState == BuzzState.OPEN) {
                    Spacer(Modifier.height(8.dp))
                    Pill(
                        text = "بانتظار بزّة من أحد الفريقين",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
        StrikeFlash(strikes = state.strikes)
    }
}

/** المضيف بيقدر يحكم بس لما يكون في فريق مستنّي جواب منه. */
private fun GameState.canJudge(): Boolean = when (phase) {
    RoundPhase.FACE_OFF -> buzzedTeam() != null
    RoundPhase.FACE_OFF_SECOND, RoundPhase.PLAY, RoundPhase.STEAL -> true
    else -> false
}

private fun GameState.nextButtonLabel(): String = when {
    !isLastRound -> "الجولة الجاية"
    fastMoneyQuestions.isNotEmpty() -> "الجولة السريعة"
    else -> "إنهاء اللعبة"
}

@Preview(showBackground = true, heightDp = 800)
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
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "النجوم", score = 140, connected = true),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الصقور", score = 95, connected = true)
                ),
                phase = RoundPhase.PLAY,
                controllingTeam = TeamId.TEAM_1,
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
