package com.feudparty.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.components.inkColor
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState

/** النتيجة النهائية — منصة تتويج، كونفيتي، ولوحة ذهبية للفائز. */
@Composable
fun GameOverScreen(
    state: GameState,
    onBackHome: (() -> Unit)? = null
) {
    val one = state.teams[TeamId.TEAM_1]
    val two = state.teams[TeamId.TEAM_2]
    val winner = state.leadingTeam
    val winnerName = winner?.let { state.teams[it]?.name }

    Box {
        StageBackground(contentPadding = PaddingValues(horizontal = 30.dp, vertical = 18.dp)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(6.dp))
                CartoonSurface(color = FeudColors.gold, corner = 20.dp, shadow = 8.dp) {
                    Text(
                        winnerName?.let { "فاز $it 🎉" } ?: "تعادل!",
                        color = FeudColors.ink,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 30.dp, vertical = 10.dp)
                    )
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Podium(
                        name = one?.name ?: "فريق ١",
                        score = one?.score ?: 0,
                        teamId = TeamId.TEAM_1,
                        height = if (winner == TeamId.TEAM_1) 190.dp else 150.dp
                    )
                    Podium(
                        name = two?.name ?: "فريق ٢",
                        score = two?.score ?: 0,
                        teamId = TeamId.TEAM_2,
                        height = if (winner == TeamId.TEAM_2) 190.dp else 150.dp
                    )
                }

                state.fastMoney?.takeIf { it.revealed }?.let { fastMoney ->
                    Spacer(Modifier.height(10.dp))
                    Pill(
                        text = "الجولة السريعة ${fastMoney.total} / ${200}" +
                            if (fastMoney.won) " — فوز!" else "",
                        color = if (fastMoney.won) FeudColors.lime else FeudColors.pink,
                        textColor = if (fastMoney.won) FeudColors.ink else Color.White
                    )
                }

                if (onBackHome != null) {
                    Spacer(Modifier.height(14.dp))
                    PrimaryButton(
                        text = "رجوع للرئيسية",
                        onClick = onBackHome,
                        color = FeudColors.teal,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        Confetti()
    }
}

@Composable
private fun Podium(name: String, score: Int, teamId: TeamId, height: Dp) {
    CartoonSurface(
        modifier = Modifier.width(230.dp),
        color = teamId.color(),
        corner = 20.dp,
        shadow = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(name, color = teamId.inkColor(), style = MaterialTheme.typography.titleLarge)
            Text("$score", color = Color.White, style = MaterialTheme.typography.displayMedium)
        }
    }
}

/** كونفيتي بيوقع من فوق — ورق ملوّن بحدود سودا. */
@Composable
private fun Confetti(pieces: Int = 14) {
    val colors = listOf(
        FeudColors.gold,
        FeudColors.pink,
        FeudColors.teal,
        FeudColors.lime,
        FeudColors.cream
    )
    val transition = rememberInfiniteTransition(label = "confetti")

    Box(modifier = Modifier.fillMaxSize()) {
        repeat(pieces) { index ->
            val duration = 2_200 + (index % 5) * 350
            val progress by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(duration, delayMillis = index * 160, easing = LinearEasing),
                    RepeatMode.Restart
                ),
                label = "fall$index"
            )
            Box(
                modifier = Modifier
                    .offset(
                        x = (14 + index * 62).dp,
                        y = (-30 + progress * 460).dp
                    )
                    .size(width = (8 + (index % 3) * 4).dp, height = (12 + (index % 2) * 6).dp)
                    .background(colors[index % colors.size], RoundedCornerShape(3.dp))
                    .border(2.dp, FeudColors.ink, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun GameOverScreenPreview() {
    FeudPartyTheme {
        GameOverScreen(
            state = GameState(
                questions = listOf(Question("q", "س", emptyList(), "عام")),
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "النجوم", 320),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الصقور", 275)
                ),
                gameOver = true
            ),
            onBackHome = {}
        )
    }
}
