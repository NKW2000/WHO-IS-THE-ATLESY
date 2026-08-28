package com.feudparty.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.app.ui.theme.TeamColors
import com.feudparty.core.game.Answer
import com.feudparty.core.game.BuzzState
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState

/** لوحة المضيف — بس هون بينحكم صح/غلط، وبس من هون بتتغير الحالة. */
@Composable
fun HostGameBoardScreen(
    state: GameState,
    onCorrect: (Int) -> Unit,
    onWrong: () -> Unit,
    onNext: () -> Unit
) {
    val buzzedTeam = state.buzzedTeam()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        ScoreBar(state)
        Spacer(Modifier.height(16.dp))

        Text(
            "سؤال ${state.currentQuestionIndex + 1} من ${state.questions.size}",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(4.dp))
        Text(
            state.currentQuestion?.text ?: "انتهت الأسئلة",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(16.dp))

        BuzzBanner(state, buzzedTeam)
        Spacer(Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            val answers = state.currentQuestion?.answers.orEmpty()
            items(answers.size) { index ->
                AnswerRow(
                    position = index + 1,
                    answer = answers[index],
                    canJudge = buzzedTeam != null && !answers[index].revealed,
                    onCorrect = { onCorrect(index) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onWrong,
                enabled = buzzedTeam != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("غلط")
            }
            Spacer(Modifier.width(12.dp))
            Button(onClick = onNext, modifier = Modifier.weight(1f)) {
                Text(if (state.isLastQuestion) "إنهاء اللعبة" else "السؤال التالي")
            }
        }
    }
}

@Composable
private fun ScoreBar(state: GameState) {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf(TeamId.TEAM_1, TeamId.TEAM_2).forEachIndexed { index, teamId ->
            val team = state.teams[teamId] ?: return@forEachIndexed
            if (index > 0) Spacer(Modifier.width(12.dp))
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = teamId.color().copy(alpha = if (team.connected) 1f else 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(team.name, color = Color.White, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${team.score}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!team.connected) {
                        Text("انقطع الاتصال", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun BuzzBanner(state: GameState, buzzedTeam: TeamId?) {
    val text = when {
        state.gameOver -> "انتهت اللعبة"
        buzzedTeam != null -> "${state.teams[buzzedTeam]?.name ?: ""} بزّ أول — احكم عالجواب"
        state.roundOver -> "انكشفت كل الأجوبة — انتقل للسؤال التالي"
        else -> "الزر مفتوح للفريقين"
    }
    val background = buzzedTeam?.color() ?: MaterialTheme.colorScheme.surfaceVariant
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(
                text,
                color = if (buzzedTeam != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun AnswerRow(
    position: Int,
    answer: Answer,
    canJudge: Boolean,
    onCorrect: () -> Unit
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // المضيف بيشوف الجواب دايماً — هو اللي لازم يحكم عليه.
                Text("$position. ${answer.text}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (answer.revealed) "مكشوف" else "مخفي عن الفرق",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text("${answer.points}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(12.dp))
            Button(onClick = onCorrect, enabled = canJudge) { Text("صح") }
        }
    }
}

private fun GameState.buzzedTeam(): TeamId? = when (buzzState) {
    BuzzState.LOCKED_TEAM_1 -> TeamId.TEAM_1
    BuzzState.LOCKED_TEAM_2 -> TeamId.TEAM_2
    else -> null
}

private val GameState.isLastQuestion: Boolean
    get() = currentQuestionIndex >= questions.lastIndex

private fun TeamId.color(): Color = when (this) {
    TeamId.TEAM_1 -> TeamColors.team1
    TeamId.TEAM_2 -> TeamColors.team2
}

@Preview(showBackground = true, heightDp = 720)
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
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "النجوم", score = 40, connected = true),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الصقور", connected = true)
                ),
                buzzState = BuzzState.LOCKED_TEAM_2
            ),
            onCorrect = {},
            onWrong = {},
            onNext = {}
        )
    }
}
