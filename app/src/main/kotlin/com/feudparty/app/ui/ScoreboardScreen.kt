package com.feudparty.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SpinningRays
import com.feudparty.app.ui.components.appear
import com.feudparty.app.ui.components.drop
import com.feudparty.app.ui.components.rememberShowClock
import com.feudparty.app.ui.components.rise
import com.feudparty.app.ui.components.thump
import com.feudparty.app.ui.components.wipe
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.components.inkColor
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState

/**
 * النتيجة بين الجولات — نفس مشهد «نهاية الجولة» بملف التصميم
 * (`مين الأطليسي - Play & Pass`): العنوان بيهبط، اللوحان بيطلعان من تحت،
 * الأرقام بتعدّ لفوق مع أعمدتها، وبعدين تاج ذهبي للمتقدّم ولافتة بتكنس.
 * بتظهر عند المضيف وعند كل اللاعبين — بس المضيف عنده زر الجولة الجاية.
 */
private const val COUNT_START = 0.5f
private const val COUNT_TIME = 0.9f
private const val CROWN_AT = 1.5f
private const val BANNER_AT = 1.56f

@Composable
fun ScoreboardScreen(
    state: GameState,
    onContinue: (() -> Unit)? = null
) {
    val roundNumber = state.currentQuestionIndex + 1
    val award = state.lastAward
    val t by rememberShowClock(key = roundNumber, cap = 4f)
    val counted = ((t - COUNT_START) / COUNT_TIME).coerceIn(0f, 1f)
    val scores = TeamId.entries.associateWith { state.teams[it]?.score ?: 0 }
    val top = scores.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val leader = when {
        scores.getValue(TeamId.TEAM_1) == scores.getValue(TeamId.TEAM_2) -> null
        scores.getValue(TeamId.TEAM_1) > scores.getValue(TeamId.TEAM_2) -> TeamId.TEAM_1
        else -> TeamId.TEAM_2
    }

    val short = shortSide()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth
        SpinningRays(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "نتيجة الجولة ${roundNumber.ar()}/${state.questions.size.ar()}",
                color = FeudColors.gold,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.graphicsLayer {
                    translationY = drop(t, 0.04f) * size.height
                    alpha = appear(t, 0.04f, 0.08f)
                }
            )

            if (award != null) {
                Spacer(Modifier.height(6.dp))
                val teamName = state.teams[award.teamId]?.name.orEmpty()
                Text(
                    if (award.stolen) {
                        "سرقة! $teamName أخد ${award.points.ar()}"
                    } else {
                        "$teamName أخد ${award.points.ar()}"
                    },
                    color = if (award.stolen) FeudColors.pink else FeudColors.lime,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(10.dp))

            if (isPortrait()) {
                // طولي: لوح فوق لوح، كل واحد سطر واحد بالاسم والرقم.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(
                        14.dp,
                        Alignment.CenterVertically
                    )
                ) {
                    TeamId.entries.forEachIndexed { index, teamId ->
                        TeamPanel(
                            name = state.teams[teamId]?.name.orEmpty(),
                            score = scores.getValue(teamId),
                            shown = (scores.getValue(teamId) * counted).toInt(),
                            share = (scores.getValue(teamId).toFloat() / top) * counted,
                            crowned = leader == teamId,
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
                            crowned = leader == teamId,
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

            Spacer(Modifier.height(10.dp))

            // لافتة «مين بالمقدمة» بتكنس عرض الشاشة زي التصميم.
            LeadBanner(
                text = when (leader) {
                    null -> "تعادل"
                    else -> "${state.teams[leader]?.name.orEmpty()} بالمقدمة"
                },
                width = w,
                height = (short * 0.14f).coerceIn(48.dp, 74.dp),
                offsetFraction = wipe(t, BANNER_AT)
            )

            Spacer(Modifier.height(10.dp))

            if (onContinue != null) {
                PrimaryButton(
                    text = if (state.isLastRound) "النتيجة النهائية" else "الجولة الجاية",
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    "بانتظار المضيف يبلّش الجولة الجاية",
                    color = FeudColors.textMuted,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

/** لوح فريق: بيطلع من تحت، رقمه بيعدّ، وعموده بيكبر معه. */
@Composable
private fun TeamPanel(
    name: String,
    score: Int,
    shown: Int,
    share: Float,
    crowned: Boolean,
    teamId: TeamId,
    t: Float,
    delay: Float,
    modifier: Modifier = Modifier,
    // بالطولي اللوح بيصير عريض: الاسم والرقم بسطر واحد.
    wide: Boolean = false
) {
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer {
                translationY = rise(t, delay) * size.height
                alpha = appear(t, delay, 0.1f)
            }
            .clip(RoundedCornerShape(18.dp))
            .background(teamId.color())
    ) {
        // الرقم بياخد قياسه من أصغر بُعد باللوح — حتى ما ينفجر بالطولي.
        val basis = minOf(maxHeight, maxWidth)

        if (crowned) {
            if (wide) {
                // بالطولي المتقدّم بيضوي بإطار ذهبي بينبض — زي التصميم.
                val glow = ((t - CROWN_AT) * 1.4f).coerceAtLeast(0f)
                val pulse = 0.6f + 0.4f * kotlin.math.sin(glow * 4.5f)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            width = 5.dp,
                            color = FeudColors.gold.copy(
                                alpha = if (t > CROWN_AT) 0.55f + 0.45f * pulse else 0f
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                )
            } else {
                // تاج المتقدّم: شريط ذهبي بينط فوق اللوح.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(12.dp)
                        .graphicsLayer { scaleY = thump(t, CROWN_AT) }
                        .background(FeudColors.gold)
                )
            }
        }

        val scoreColor =
            if (teamId == TeamId.TEAM_1) teamId.inkColor() else FeudColors.cream
        val scoreSize = if (wide) basis * 0.26f else basis * 0.34f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (wide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        name,
                        color = teamId.inkColor(),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        shown.coerceAtMost(score).ar(),
                        color = scoreColor,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = with(density) { scoreSize.toSp() },
                            lineHeight = with(density) { (scoreSize * 1.06f).toSp() }
                        ),
                        maxLines = 1
                    )
                }
            } else {
                Text(
                    name,
                    color = teamId.inkColor(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    shown.coerceAtMost(score).ar(),
                    color = scoreColor,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = with(density) { scoreSize.toSp() },
                        lineHeight = with(density) { (scoreSize * 1.06f).toSp() }
                    ),
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (wide) 1f else 0.64f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(FeudColors.ink.copy(alpha = 0.28f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(share.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(FeudColors.ink)
                )
            }
        }
    }
}

/** لافتة ذهبية بتكنس عرض الشاشة وبتقول مين بالمقدمة. */
@Composable
private fun LeadBanner(
    text: String,
    width: Dp,
    height: Dp,
    offsetFraction: Float
) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .offset(x = width * offsetFraction)
            .background(FeudColors.gold),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = FeudColors.ink,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = with(density) { (height * 0.45f).toSp() }
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
