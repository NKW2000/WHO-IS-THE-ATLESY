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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.graphicsLayer
import com.feudparty.app.ui.components.SpinningRays
import com.feudparty.app.ui.components.appear
import com.feudparty.app.ui.components.drop
import com.feudparty.app.ui.components.rememberShowClock
import com.feudparty.app.feedback.Cue
import com.feudparty.app.feedback.FireworkCues
import com.feudparty.app.feedback.SceneCue
import com.feudparty.app.ui.components.wipe
import androidx.compose.foundation.layout.BoxWithConstraints
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
import com.feudparty.app.ui.components.Fireworks
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.components.inkColor
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudShape
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState

/**
 * النتيجة النهائية — نفس لغة «نتيجة الجولة»: أشعة بتلف، اللوحان بيطلعان
 * من تحت وأرقامهم بتعدّ مع أعمدتها، الفائز بيضوي، ولافتة ذهبية بتكنس
 * باسمه، وفوق الكل قصاصات وألعاب نارية.
 */
@Composable
fun GameOverScreen(
    state: GameState,
    onBackHome: (() -> Unit)? = null,
    onBackToLobby: (() -> Unit)? = null
) {
    val winner = state.leadingTeam
    val winnerName = winner?.let { state.teams[it]?.name }
    val scores = TeamId.entries.associateWith { state.teams[it]?.score ?: 0 }
    val top = scores.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val t by rememberShowClock(key = "final", cap = 4f)
    // هون الفانفير لحالها بتحمل المشهد — ما منحط دقّات العدّ فوقها حتى
    // ما يتوسّخ الصوت. الطقّات بتجي بعد ما تخلص.
    SceneCue(Cue.FANFARE, key = "final")
    FireworkCues(key = "final")
    val counted = ((t - 0.5f) / 0.9f).coerceIn(0f, 1f)
    val portrait = isPortrait()
    val short = shortSide()

    Box {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val w = maxWidth
            SpinningRays(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "النتيجة النهائية",
                    color = FeudColors.gold,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.graphicsLayer {
                        translationY = drop(t, 0.04f) * size.height
                        alpha = appear(t, 0.04f, 0.08f)
                    }
                )

                Spacer(Modifier.height(12.dp))

                if (portrait) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
                    ) {
                        TeamId.entries.forEachIndexed { index, teamId ->
                            TeamPanel(
                                name = state.teams[teamId]?.name.orEmpty(),
                                score = scores.getValue(teamId),
                                shown = (scores.getValue(teamId) * counted).toInt(),
                                share = (scores.getValue(teamId).toFloat() / top) * counted,
                                crowned = winner == teamId,
                                teamId = teamId,
                                t = t,
                                delay = if (index == 0) 0.2f else 0.32f,
                                wide = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        TeamId.entries.forEachIndexed { index, teamId ->
                            TeamPanel(
                                name = state.teams[teamId]?.name.orEmpty(),
                                score = scores.getValue(teamId),
                                shown = (scores.getValue(teamId) * counted).toInt(),
                                share = (scores.getValue(teamId).toFloat() / top) * counted,
                                crowned = winner == teamId,
                                teamId = teamId,
                                t = t,
                                delay = if (index == 0) 0.2f else 0.32f,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                LeadBanner(
                    text = winnerName?.let { "فاز $it" } ?: "تعادل!",
                    width = w,
                    height = (short * 0.14f).coerceIn(52.dp, 84.dp),
                    offsetFraction = wipe(t, 1.56f)
                )

                if (onBackHome != null || onBackToLobby != null) {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (onBackToLobby != null) {
                            PrimaryButton(
                                text = "رجوع للوبي",
                                onClick = onBackToLobby,
                                color = FeudColors.lime,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (onBackHome != null) {
                            PrimaryButton(
                                text = "الرئيسية",
                                onClick = onBackHome,
                                color = FeudColors.teal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
        Confetti()
        // ألعاب نارية فوق القصاصات — نفس ألوان اللعبة وحدودها الحبرية.
        Fireworks()
    }
}

@Composable
private fun Podium(
    name: String,
    score: Int,
    teamId: TeamId,
    height: Dp,
    modifier: Modifier = Modifier
) {
    CartoonSurface(
        modifier = modifier,
        color = teamId.color(),
        corner = FeudShape.block,
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
            Text(score.ar(), color = Color.White, style = MaterialTheme.typography.displayMedium)
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
