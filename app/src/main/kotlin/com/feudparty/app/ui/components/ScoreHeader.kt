package com.feudparty.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.core.game.GameState
import com.feudparty.core.game.TeamId

fun TeamId.color(): Color = when (this) {
    TeamId.TEAM_1 -> FeudColors.team1
    TeamId.TEAM_2 -> FeudColors.team2
}

/** رأس الشاشة: نقاط الفريقين وبينهم كوم نقاط الجولة الحالية. */
@Composable
fun ScoreHeader(
    state: GameState,
    modifier: Modifier = Modifier,
    showPot: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamScoreCard(state, TeamId.TEAM_1, Modifier.weight(1f))
        if (showPot) {
            Spacer(Modifier.width(10.dp))
            PotCard(pot = state.pot, multiplier = state.multiplier)
            Spacer(Modifier.width(10.dp))
        } else {
            Spacer(Modifier.width(12.dp))
        }
        TeamScoreCard(state, TeamId.TEAM_2, Modifier.weight(1f))
    }
}

@Composable
private fun TeamScoreCard(state: GameState, teamId: TeamId, modifier: Modifier = Modifier) {
    val team = state.teams[teamId] ?: return
    val active = state.activeTeam == teamId
    val color = teamId.color()
    val score by animateIntAsState(targetValue = team.score, animationSpec = tween(450), label = "score")

    val glow = if (active) {
        val transition = rememberInfiniteTransition(label = "glow")
        transition.animateFloat(
            initialValue = 0.45f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(750, easing = LinearEasing), RepeatMode.Reverse),
            label = "glowAlpha"
        ).value
    } else {
        0.35f
    }

    Column(
        modifier = modifier
            .background(color.copy(alpha = if (active) 0.18f else 0.08f), RoundedCornerShape(14.dp))
            .border(2.dp, color.copy(alpha = glow), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            team.name,
            color = color,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            "$score",
            color = FeudColors.text,
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            if (team.connected) (if (active) "دورهم" else " ") else "انقطع الاتصال",
            color = if (team.connected) color else FeudColors.strike,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun PotCard(pot: Int, multiplier: Int) {
    val shown by animateIntAsState(targetValue = pot, animationSpec = tween(350), label = "pot")
    Column(
        modifier = Modifier
            .background(FeudColors.panelDark, RoundedCornerShape(14.dp))
            .border(2.dp, FeudColors.gold, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("نقاط الجولة", color = FeudColors.goldDim, style = MaterialTheme.typography.labelSmall)
        Text("$shown", color = FeudColors.gold, style = MaterialTheme.typography.headlineMedium)
        if (multiplier > 1) {
            Box(
                modifier = Modifier
                    .background(FeudColors.gold, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 1.dp)
            ) {
                Text(
                    "×$multiplier",
                    color = FeudColors.deepNavy,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        } else {
            Spacer(Modifier.height(4.dp))
        }
    }
}
