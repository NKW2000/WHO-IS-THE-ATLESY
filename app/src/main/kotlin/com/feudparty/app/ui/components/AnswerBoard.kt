package com.feudparty.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudBrushes
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.core.game.Answer

/**
 * لوح الأجوبة — كل خانة بتنقلب لما تنكشف، تماماً زي لوح البرنامج.
 *
 * @param revealHiddenText المضيف بس بيشوف نص الجواب المخفي (لأنه بيحكم عليه).
 * @param onSlotClick لما ينضغط على خانة مخفية — بيستعملها المضيف كـ«صح».
 */
@Composable
fun AnswerBoard(
    answers: List<Answer>,
    modifier: Modifier = Modifier,
    revealHiddenText: Boolean = false,
    enabledSlots: Boolean = false,
    startIndex: Int = 0,
    slotHeight: Dp = 62.dp,
    onSlotClick: ((Int) -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        answers.forEachIndexed { offset, answer ->
            val index = startIndex + offset
            AnswerSlot(
                position = index + 1,
                answer = answer,
                revealHiddenText = revealHiddenText,
                enabled = enabledSlots && !answer.revealed && onSlotClick != null,
                height = slotHeight,
                onClick = { onSlotClick?.invoke(index) }
            )
        }
    }
}

/** نفس اللوح بس بعمودين — شكل شاشة البرنامج بالوضع الأفقي. */
@Composable
fun AnswerBoardColumns(
    answers: List<Answer>,
    modifier: Modifier = Modifier,
    revealHiddenText: Boolean = false,
    enabledSlots: Boolean = false,
    slotHeight: Dp = 56.dp,
    onSlotClick: ((Int) -> Unit)? = null
) {
    val half = (answers.size + 1) / 2
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnswerBoard(
            answers = answers.take(half),
            modifier = Modifier.weight(1f),
            revealHiddenText = revealHiddenText,
            enabledSlots = enabledSlots,
            startIndex = 0,
            slotHeight = slotHeight,
            onSlotClick = onSlotClick
        )
        if (answers.size > half) {
            AnswerBoard(
                answers = answers.drop(half),
                modifier = Modifier.weight(1f),
                revealHiddenText = revealHiddenText,
                enabledSlots = enabledSlots,
                startIndex = half,
                slotHeight = slotHeight,
                onSlotClick = onSlotClick
            )
        }
    }
}

@Composable
private fun AnswerSlot(
    position: Int,
    answer: Answer,
    revealHiddenText: Boolean,
    enabled: Boolean,
    height: Dp,
    onClick: () -> Unit
) {
    val flip by animateFloatAsState(
        targetValue = if (answer.revealed) 180f else 0f,
        animationSpec = tween(durationMillis = 520),
        label = "flip"
    )
    val density = LocalDensity.current.density
    val showBack = flip > 90f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                rotationX = flip
                cameraDistance = 14f * density
            }
            .background(
                brush = if (showBack) FeudBrushes.tileRevealed else FeudBrushes.tile,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                BorderStroke(
                    2.dp,
                    when {
                        showBack -> FeudColors.gold
                        enabled -> FeudColors.gold.copy(alpha = 0.55f)
                        else -> FeudColors.goldDim.copy(alpha = 0.35f)
                    }
                ),
                RoundedCornerShape(12.dp)
            )
            .let { if (enabled) it.clickable(onClick = onClick) else it }
    ) {
        // الوجه الخلفي بينقلب مرة تانية حتى يضل النص معتدل بعد الدوران.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .graphicsLayer { if (showBack) rotationX = 180f },
            contentAlignment = Alignment.CenterStart
        ) {
            if (showBack) {
                RevealedFace(position, answer)
            } else {
                HiddenFace(position, answer, revealHiddenText, enabled)
            }
        }
    }
}

@Composable
private fun RevealedFace(position: Int, answer: Answer) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SlotNumber(position, active = true)
        Spacer(Modifier.width(12.dp))
        Text(
            answer.text,
            color = FeudColors.text,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        PointsChip(answer.points)
    }
}

@Composable
private fun HiddenFace(
    position: Int,
    answer: Answer,
    revealHiddenText: Boolean,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SlotNumber(position, active = false)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (revealHiddenText) {
                Text(
                    answer.text,
                    color = FeudColors.textMuted,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (enabled) "اضغط للكشف" else "مخفي عن الفرق",
                    color = FeudColors.goldDim,
                    style = MaterialTheme.typography.labelSmall
                )
            } else {
                Text(
                    "• • • • • •",
                    color = FeudColors.textMuted.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        if (revealHiddenText) {
            Spacer(Modifier.width(8.dp))
            PointsChip(answer.points, dim = true)
        }
    }
}

@Composable
private fun SlotNumber(position: Int, active: Boolean) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(
                if (active) FeudColors.gold else FeudColors.gold.copy(alpha = 0.18f),
                CircleShape
            )
            .border(1.dp, FeudColors.gold.copy(alpha = 0.7f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$position",
            color = if (active) FeudColors.deepNavy else FeudColors.gold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun PointsChip(points: Int, dim: Boolean = false) {
    val color = if (dim) FeudColors.goldDim else FeudColors.gold
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            "$points",
            color = color,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    }
}
