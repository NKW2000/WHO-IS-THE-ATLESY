package com.feudparty.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.ar
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.core.game.GameState
import com.feudparty.core.game.TeamId

/** بطاقة السؤال — لوح كريمي عريض زي شاشة البرنامج. */
@Composable
fun QuestionCard(
    round: Int,
    totalRounds: Int,
    category: String?,
    question: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(tween(320)) { it / 4 } + fadeIn(tween(260))
    ) {
        GoldPanel(modifier = modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                if (category != null) {
                    Text(
                        "$category · جولة ${round.ar()}/${totalRounds.ar()}",
                        color = FeudColors.ink.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    question,
                    color = FeudColors.ink,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** شريط الحالة تحت السؤال — بياخد لون الفريق اللي عليه الدور. */
@Composable
fun StatusBanner(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
    filled: Boolean = true
) {
    CartoonSurface(
        modifier = modifier.fillMaxWidth(),
        color = if (filled) accent else FeudColors.ink.copy(alpha = 0.45f),
        borderWidth = 4.dp,
        corner = 16.dp,
        shadow = 5.dp
    ) {
        Text(
            text,
            color = if (filled) FeudColors.ink else FeudColors.textMuted,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp)
        )
    }
}

/** إعلان نقاط نهاية الجولة — بينط بضربة. */
@Composable
fun AwardBanner(state: GameState, modifier: Modifier = Modifier) {
    val award = state.lastAward
    AnimatedVisibility(
        visible = award != null && state.roundOver,
        enter = scaleIn(initialScale = 1.6f, animationSpec = tween(280)) + fadeIn(tween(180)),
        exit = fadeOut(tween(160)),
        modifier = modifier
    ) {
        if (award == null) return@AnimatedVisibility
        val teamName = state.teams[award.teamId]?.name ?: ""
        CartoonSurface(
            modifier = Modifier.fillMaxWidth(),
            color = if (award.stolen) FeudColors.pink else FeudColors.gold,
            borderWidth = 4.dp,
            corner = 16.dp,
            shadow = 5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (award.stolen) "سرقة! $teamName +${award.points.ar()}"
                    else "$teamName +${award.points.ar()}",
                    color = if (award.stolen) Color.White else FeudColors.ink,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun accentFor(teamId: TeamId?): Color = teamId?.color() ?: FeudColors.gold

/** فراغ صغير بين عناصر الشاشة. */
@Composable
fun BannerSpacer() = Box(modifier = Modifier.height(10.dp))
