package com.feudparty.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.core.game.Award
import com.feudparty.core.game.GameState
import com.feudparty.core.game.TeamId

/** بطاقة السؤال — العنوان الأكبر بالشاشة. */
@Composable
fun QuestionCard(
    round: Int,
    totalRounds: Int,
    category: String?,
    question: String,
    modifier: Modifier = Modifier
) {
    GoldPanel(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "الجولة $round من $totalRounds",
                    color = FeudColors.goldDim,
                    style = MaterialTheme.typography.labelLarge
                )
                if (!category.isNullOrBlank()) {
                    Text(category, color = FeudColors.goldDim, style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(8.dp))
            GoldDivider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            Text(
                question,
                color = FeudColors.text,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** شريط الحالة: مين دوره وشو المطلوب — بيتلوّن بلون الفريق النشط. */
@Composable
fun StatusBanner(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
            .border(2.dp, accent, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = accent,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

/** بانر نتيجة الجولة — «الفريق الفلاني خد ١٢٠ نقطة» (أو «سرقها»). */
@Composable
fun AwardBanner(
    state: GameState,
    modifier: Modifier = Modifier
) {
    val award: Award? = state.lastAward.takeIf { state.roundOver }
    AnimatedVisibility(
        visible = award != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val shown = award ?: return@AnimatedVisibility
        val color = shown.teamId.color()
        val name = state.teams[shown.teamId]?.name ?: ""
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                .border(2.dp, color, RoundedCornerShape(14.dp))
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (shown.stolen) "سرقة ناجحة!" else "الجولة لـ $name",
                    color = color,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    if (shown.stolen) "$name خد ${shown.points} نقطة" else "+${shown.points} نقطة",
                    color = FeudColors.text,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

/** لون الفريق أو الذهبي إذا ما في فريق نشط. */
fun accentFor(teamId: TeamId?): Color = teamId?.color() ?: FeudColors.gold
