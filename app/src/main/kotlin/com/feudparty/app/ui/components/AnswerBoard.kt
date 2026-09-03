package com.feudparty.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.ar
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.Answer

/**
 * لوح الأجوبة. الخانة المخفية كريمية بعلامة «؟ ؟ ؟» للاعبين، ولما تنكشف
 * بتنقلب لخضرا بحركة flip زي البرنامج.
 */
@Composable
fun AnswerBoard(
    answers: List<Answer>,
    modifier: Modifier = Modifier,
    revealHiddenText: Boolean = false,
    enabledSlots: Boolean = false,
    startIndex: Int = 0,
    slotHeight: Dp = 58.dp,
    onSlotClick: ((Int) -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
    slotHeight: Dp = 54.dp,
    onSlotClick: ((Int) -> Unit)? = null
) {
    val half = (answers.size + 1) / 2
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
    // الانقلاب: الخانة بتلف على محورها الأفقي أول ما تنكشف.
    val flip by animateFloatAsState(
        targetValue = if (answer.revealed) 0f else -90f,
        animationSpec = tween(durationMillis = 380),
        label = "flip$position"
    )
    val revealed = answer.revealed

    CartoonSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                // بس المكشوفة بتتحرك؛ المخفية ثابتة.
                if (revealed) {
                    rotationX = flip
                    cameraDistance = 14f * density
                }
            },
        color = if (revealed) FeudColors.team1 else FeudColors.cream,
        borderWidth = 4.dp,
        corner = 16.dp,
        shadow = 5.dp,
        onClick = if (enabled) onClick else null,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SlotNumber(position = position, revealed = revealed)
            Spacer(Modifier.width(10.dp))
            Text(
                text = when {
                    revealed -> answer.text
                    revealHiddenText -> answer.text
                    else -> "؟ ؟ ؟"
                },
                color = when {
                    revealed -> Color.White
                    revealHiddenText -> FeudColors.ink
                    else -> FeudColors.ink.copy(alpha = 0.45f)
                },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                answer.points.ar(),
                color = if (revealed) FeudColors.cream else FeudColors.pink,
                style = MaterialTheme.typography.titleLarge
            )
            if (enabled || revealed) {
                Spacer(Modifier.width(10.dp))
                JudgeChip(revealed = revealed)
            }
        }
    }
}

@Composable
private fun SlotNumber(position: Int, revealed: Boolean) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(
                if (revealed) FeudColors.team1Ink else FeudColors.gold,
                RoundedCornerShape(9.dp)
            )
            .border(3.dp, FeudColors.ink, RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            position.ar(),
            color = if (revealed) FeudColors.lime else FeudColors.ink,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/** زر «صح» عند المضيف — بيصير علامة ✓ بعد الكشف. */
@Composable
private fun JudgeChip(revealed: Boolean) {
    Box(
        modifier = Modifier
            .background(
                if (revealed) FeudColors.lime else FeudColors.pink,
                RoundedCornerShape(10.dp)
            )
            .border(3.dp, FeudColors.ink, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            if (revealed) "✓" else "صح",
            color = if (revealed) FeudColors.team1Ink else Color.White,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(widthDp = 460, heightDp = 300, showBackground = true)
@Composable
private fun AnswerBoardPreview() {
    FeudPartyTheme {
        Box(modifier = Modifier.background(FeudColors.stage).padding(12.dp)) {
            AnswerBoard(
                answers = listOf(
                    Answer("يشيّكوا الموبايل", 40, revealed = true),
                    Answer("يشربوا قهوة", 30),
                    Answer("يغسلوا وجّهم", 20)
                ),
                revealHiddenText = true,
                enabledSlots = true,
                onSlotClick = {}
            )
        }
    }
}
