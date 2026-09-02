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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.AnswerBoard
import com.feudparty.app.ui.components.AwardBanner
import com.feudparty.app.ui.components.BuzzerButton
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.QuestionCard
import com.feudparty.app.ui.components.ScoreHeader
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.StatusBanner
import com.feudparty.app.ui.components.StrikeFlash
import com.feudparty.app.ui.components.StrikeRow
import com.feudparty.app.ui.components.accentFor
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.app.viewmodel.TeamViewModel.ConnectionStatus
import com.feudparty.core.game.Answer
import com.feudparty.core.game.BuzzState
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState
import com.feudparty.core.game.buzzedTeam

/**
 * شاشة الفريق — الجهاز هون زر فقط: بيبعت البزّة وبيعرض الحالة اللي بتوصل
 * من المضيف. ما بيقرر ولا بيحسب إشي محلياً، والأجوبة المخفية بتوصله
 * بدون نص أصلاً.
 */
@Composable
fun TeamBuzzScreen(
    state: GameState?,
    myTeam: TeamId?,
    status: ConnectionStatus,
    canBuzz: Boolean,
    onBuzz: () -> Unit
) {
    val accent = myTeam?.color() ?: FeudColors.gold

    Box {
        StageBackground(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ConnectionStrip(state, myTeam, status)
                Spacer(Modifier.height(10.dp))

                if (state != null) {
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
                            text = teamStatusText(state, myTeam),
                            accent = accentFor(state.activeTeam),
                            modifier = Modifier.weight(1f)
                        )
                        if (state.phase == RoundPhase.PLAY || state.phase == RoundPhase.STEAL) {
                            Spacer(Modifier.width(10.dp))
                            StrikeRow(strikes = state.strikes, size = 28.dp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    AwardBanner(state)
                    AnswerBoard(answers = state.currentQuestion?.answers.orEmpty())
                    Spacer(Modifier.height(18.dp))
                } else {
                    Spacer(Modifier.height(40.dp))
                    Text(
                        "بانتظار السؤال من المضيف",
                        color = FeudColors.textMuted,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                }

                BuzzerButton(
                    label = buzzerLabel(state, myTeam, canBuzz),
                    subLabel = buzzerSubLabel(state, myTeam),
                    enabled = canBuzz,
                    accent = accent,
                    onClick = onBuzz
                )
                Spacer(Modifier.height(16.dp))
            }
        }
        if (state != null) StrikeFlash(strikes = state.strikes)
    }
}

@Composable
private fun ConnectionStrip(state: GameState?, myTeam: TeamId?, status: ConnectionStatus) {
    val (text, color) = when (status) {
        ConnectionStatus.IDLE -> "غير متصل" to FeudColors.textMuted
        ConnectionStatus.SEARCHING -> "جاري البحث عن المضيف..." to FeudColors.gold
        ConnectionStatus.CONNECTED -> {
            val teamName = myTeam?.let { state?.teams?.get(it)?.name }
            val label = teamName?.let { "متصل — إنتوا $it" } ?: "متصل — بانتظار تخصيص الفريق"
            label to (myTeam?.color() ?: FeudColors.gold)
        }

        ConnectionStatus.DISCONNECTED -> "انقطع الاتصال بالمضيف" to FeudColors.strike
    }
    Pill(text = text, color = color, modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp))
}

private fun buzzerLabel(state: GameState?, myTeam: TeamId?, canBuzz: Boolean): String {
    if (state == null) return "استنى"
    return when {
        state.gameOver -> "انتهت"
        state.phase == RoundPhase.FACE_OFF && state.buzzedTeam() == myTeam && myTeam != null -> "بزّيت!"
        state.phase == RoundPhase.FACE_OFF && state.buzzedTeam() != null -> "سبقوكم"
        canBuzz -> "بزّ!"
        state.activeTeam == myTeam -> "دوركم"
        else -> "استنى"
    }
}

private fun buzzerSubLabel(state: GameState?, myTeam: TeamId?): String? = when {
    state == null -> null
    state.phase == RoundPhase.PLAY && state.controllingTeam == myTeam -> "جاوبوا للمضيف"
    state.phase == RoundPhase.STEAL && state.stealingTeam == myTeam -> "جواب واحد بس"
    state.phase == RoundPhase.ROUND_END -> "الجولة خلصت"
    else -> null
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun TeamBuzzScreenPreview() {
    FeudPartyTheme {
        TeamBuzzScreen(
            state = GameState(
                questions = listOf(
                    Question(
                        "q1",
                        "اذكر مكان بيروح عليه الناس بالعطلة",
                        listOf(
                            Answer("البحر", 35, revealed = true),
                            Answer("", 25),
                            Answer("", 22)
                        ),
                        "عام"
                    )
                ),
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "النجوم", score = 40, connected = true),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الصقور", connected = true)
                ),
                buzzState = BuzzState.OPEN,
                pot = 35
            ),
            myTeam = TeamId.TEAM_2,
            status = ConnectionStatus.CONNECTED,
            canBuzz = true,
            onBuzz = {}
        )
    }
}

