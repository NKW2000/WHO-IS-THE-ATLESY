package com.feudparty.app.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.GoldDivider
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.FastMoneyState
import com.feudparty.core.game.GameState
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState

/** شاشة النتيجة النهائية — بتظهر عند المضيف وعند الفرق بنفس الوقت. */
@Composable
fun GameOverScreen(
    state: GameState,
    onBackHome: (() -> Unit)? = null
) {
    val teams = state.teams.values.sortedByDescending { it.score }
    val topScore = teams.firstOrNull()?.score ?: 0
    val winners = teams.filter { it.score == topScore }
    val tie = winners.size > 1

    val transition = rememberInfiniteTransition(label = "winnerGlow")
    val glow by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "winnerScale"
    )

    StageBackground(contentPadding = PaddingValues(20.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("انتهت اللعبة", color = FeudColors.goldDim, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .graphicsLayer { scaleX = glow; scaleY = glow }
                    .background(FeudColors.gold.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .border(3.dp, FeudColors.gold, RoundedCornerShape(20.dp))
                    .padding(horizontal = 26.dp, vertical = 18.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (tie) "تعادل!" else "الفائز",
                        color = FeudColors.goldDim,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (tie) winners.joinToString(" و ") { it.name } else winners.firstOrNull()?.name ?: "—",
                        color = FeudColors.gold,
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "$topScore نقطة",
                        color = FeudColors.text,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Spacer(Modifier.height(26.dp))
            GoldDivider(Modifier.fillMaxWidth(0.6f))
            Spacer(Modifier.height(26.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf(TeamId.TEAM_1, TeamId.TEAM_2).forEachIndexed { index, teamId ->
                    val team = state.teams[teamId] ?: return@forEachIndexed
                    if (index > 0) Spacer(Modifier.width(12.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(teamId.color().copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .border(2.dp, teamId.color(), RoundedCornerShape(16.dp))
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            team.name,
                            color = teamId.color(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${team.score}",
                            color = FeudColors.text,
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                }
            }

            val fastMoney = state.fastMoney
            if (fastMoney != null && fastMoney.revealed) {
                Spacer(Modifier.height(18.dp))
                Pill(
                    text = if (fastMoney.won) {
                        "الجولة السريعة: ${fastMoney.total}/${FastMoneyState.TARGET} — نجحوا!"
                    } else {
                        "الجولة السريعة: ${fastMoney.total}/${FastMoneyState.TARGET}"
                    },
                    color = if (fastMoney.won) FeudColors.gold else FeudColors.textMuted
                )
            }

            if (onBackHome != null) {
                Spacer(Modifier.height(36.dp))
                PrimaryButton(
                    text = "رجوع للشاشة الرئيسية",
                    onClick = onBackHome,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun GameOverScreenPreview() {
    FeudPartyTheme {
        GameOverScreen(
            state = GameState(
                questions = emptyList(),
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "النجوم", score = 410),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الصقور", score = 260)
                ),
                gameOver = true
            ),
            onBackHome = {}
        )
    }
}
