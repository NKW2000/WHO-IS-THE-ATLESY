package com.feudparty.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.app.ui.theme.TeamColors
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("انتهت اللعبة", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(16.dp))
        Text(
            if (winners.size > 1) "تعادل! ${winners.joinToString(" و ") { it.name }}"
            else "الفائز: ${winners.firstOrNull()?.name ?: "—"}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(TeamId.TEAM_1, TeamId.TEAM_2).forEachIndexed { index, teamId ->
                val team = state.teams[teamId] ?: return@forEachIndexed
                if (index > 0) Spacer(Modifier.width(12.dp))
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = teamId.color())
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(team.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${team.score}",
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (onBackHome != null) {
            Spacer(Modifier.height(40.dp))
            Button(onClick = onBackHome, modifier = Modifier.fillMaxWidth()) {
                Text("رجوع للشاشة الرئيسية")
            }
        }
    }
}

private fun TeamId.color(): Color = when (this) {
    TeamId.TEAM_1 -> TeamColors.team1
    TeamId.TEAM_2 -> TeamColors.team2
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun GameOverScreenPreview() {
    FeudPartyTheme {
        GameOverScreen(
            state = GameState(
                questions = emptyList(),
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "النجوم", score = 210),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الصقور", score = 180)
                ),
                gameOver = true
            ),
            onBackHome = {}
        )
    }
}
