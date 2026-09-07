package com.feudparty.app.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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

/**
 * النتيجة بين الجولات — بتظهر عند المضيف وعند كل اللاعبين بنفس الوقت.
 * المضيف بس هو اللي عنده زر الانتقال للجولة الجاية.
 */
@Composable
fun ScoreboardScreen(
    state: GameState,
    onContinue: (() -> Unit)? = null
) {
    val award = state.lastAward
    val roundNumber = state.currentQuestionIndex + 1

    StageBackground(contentPadding = PaddingValues(horizontal = 30.dp, vertical = 20.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Pill(
                text = "انتهت الجولة ${roundNumber.ar()} من ${state.questions.size.ar()}",
                color = FeudColors.gold
            )

            if (award != null) {
                Spacer(Modifier.height(10.dp))
                val teamName = state.teams[award.teamId]?.name ?: ""
                Text(
                    if (award.stolen) {
                        "سرقة! $teamName أخد ${award.points.ar()}"
                    } else {
                        "$teamName أخد ${award.points.ar()}"
                    },
                    color = if (award.stolen) FeudColors.pink else FeudColors.lime,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                TeamId.entries.forEach { teamId ->
                    TeamScore(
                        state = state,
                        teamId = teamId,
                        leading = state.leadingTeam == teamId,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (onContinue != null) {
                Spacer(Modifier.height(14.dp))
                PrimaryButton(
                    text = if (state.isLastRound) "النتيجة النهائية" else "الجولة الجاية",
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Spacer(Modifier.height(14.dp))
                Text(
                    "بانتظار المضيف يبلّش الجولة الجاية",
                    color = FeudColors.textMuted,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun TeamScore(
    state: GameState,
    teamId: TeamId,
    leading: Boolean,
    modifier: Modifier = Modifier
) {
    val team = state.teams[teamId] ?: return
    val score by animateIntAsState(
        targetValue = team.score,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "score${teamId.name}"
    )

    CartoonSurface(
        modifier = modifier,
        color = teamId.color(),
        corner = 22.dp,
        shadow = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                team.name,
                color = teamId.inkColor(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Text(
                score.ar(),
                color = Color.White,
                style = MaterialTheme.typography.displayLarge
            )
            if (leading) {
                Box(modifier = Modifier.padding(top = 6.dp)) {
                    Pill(text = "متقدّم", color = FeudColors.gold)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun ScoreboardScreenPreview() {
    FeudPartyTheme {
        ScoreboardScreen(
            state = GameState(
                questions = listOf(Question("q", "س", emptyList(), "عام")),
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "الفريق الأخضر", 140),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الفريق الأزرق", 95)
                ),
                lastAward = com.feudparty.core.game.Award(TeamId.TEAM_1, 140)
            ),
            onContinue = {}
        )
    }
}
