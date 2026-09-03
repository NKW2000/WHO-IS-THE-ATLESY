package com.feudparty.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.ar
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.core.game.GameState
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId

fun TeamId.color(): Color = when (this) {
    TeamId.TEAM_1 -> FeudColors.team1
    TeamId.TEAM_2 -> FeudColors.team2
}

fun TeamId.inkColor(): Color = when (this) {
    TeamId.TEAM_1 -> FeudColors.team1Ink
    TeamId.TEAM_2 -> FeudColors.team2Ink
}

/** رأس الشاشة: نتيجة الفريقين على الجناحين، والجولة والأخطاء بالنص. */
@Composable
fun ScoreHeader(state: GameState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamScoreCard(state, TeamId.TEAM_1, Modifier.width(210.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "جولة ${(state.currentQuestionIndex + 1).ar()} من ${state.questions.size.ar()}" +
                    if (state.multiplier > 1) " · ×${state.multiplier.ar()}" else "",
                color = FeudColors.gold,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
            if (state.phase == RoundPhase.PLAY || state.phase == RoundPhase.STEAL) {
                StrikeRow(strikes = state.strikes, size = 34.dp)
            }
            if (state.pot > 0) {
                Pill(text = "نقاط الجولة ${state.pot.ar()}", color = FeudColors.gold)
            }
        }
        TeamScoreCard(state, TeamId.TEAM_2, Modifier.width(210.dp))
    }
}

@Composable
private fun TeamScoreCard(state: GameState, teamId: TeamId, modifier: Modifier = Modifier) {
    val team = state.teams[teamId] ?: return
    val active = state.activeTeam == teamId
    // النتيجة بتنبض لما تزيد.
    val score by animateIntAsState(targetValue = team.score, label = "score${teamId.name}")
    val bump by animateFloatAsState(
        targetValue = if (active) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bump${teamId.name}"
    )

    CartoonSurface(
        modifier = modifier.scale(bump),
        color = if (team.connected) teamId.color() else teamId.color().copy(alpha = 0.45f),
        corner = 20.dp,
        shadow = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    team.name,
                    color = teamId.inkColor(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (active) {
                    Text(
                        "دورهم",
                        color = teamId.inkColor().copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                score.ar(),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

/** بطاقة نتيجة مصغّرة — لشاشات اللاعبين. */
@Composable
fun MiniScore(state: GameState, teamId: TeamId, modifier: Modifier = Modifier) {
    val team = state.teams[teamId] ?: return
    CartoonSurface(
        modifier = modifier,
        color = teamId.color(),
        borderWidth = 4.dp,
        corner = 14.dp,
        shadow = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                team.name,
                color = teamId.inkColor(),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Box(modifier = Modifier.padding(start = 8.dp)) {
                Text(team.score.ar(), color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
