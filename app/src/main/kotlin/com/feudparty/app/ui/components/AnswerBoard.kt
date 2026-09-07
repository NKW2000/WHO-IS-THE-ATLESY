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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
    showHiddenPoints: Boolean = true,
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
                showHiddenPoints = showHiddenPoints,
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
    showHiddenPoints: Boolean = true,
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
            showHiddenPoints = showHiddenPoints,
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
                showHiddenPoints = showHiddenPoints,
                enabledSlots = enabledSlots,
                startIndex = half,
                slotHeight = slotHeight,
                onSlotClick = onSlotClick
            )
        }
    }
}

/**
 * اللوح كشبكة — تلات خانات بالسطر. عدد الخانات ثابت ([slots]) مهما كان
 * عدد الأجوبة: السؤال اللي أجوبته أقل بيضل لوحه كامل والخانات الزايدة
 * بتضل فاضية، زي لوح البرنامج. بياخد كل الارتفاع المتاح فما بيحتاج تمرير.
 */
@Composable
fun AnswerBoardGrid(
    answers: List<Answer>,
    modifier: Modifier = Modifier,
    columns: Int = 3,
    slots: Int = 9,
    revealHiddenText: Boolean = false,
    showHiddenPoints: Boolean = true,
    enabledSlots: Boolean = false,
    spacing: Dp = 8.dp,
    onSlotClick: ((Int) -> Unit)? = null
) {
    val padded: List<Answer?> = List(maxOf(slots, answers.size)) { answers.getOrNull(it) }
    val rows = padded.chunked(columns)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                row.forEachIndexed { columnIndex, answer ->
                    val index = rowIndex * columns + columnIndex
                    if (answer == null) {
                        EmptySlot(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    } else {
                        AnswerSlot(
                            position = index + 1,
                            answer = answer,
                            revealHiddenText = revealHiddenText,
                            showHiddenPoints = showHiddenPoints,
                            enabled = enabledSlots && !answer.revealed && onSlotClick != null,
                            height = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { onSlotClick?.invoke(index) }
                        )
                    }
                }
                // سطر ناقص: منترك مكان الخانات الفاضية حتى يضل العرض ثابت.
                repeat(columns - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

/** خانة ما إلها جواب بهاد السؤال — بتضل فاضية حتى يضل اللوح بنفس الشكل. */
@Composable
private fun EmptySlot(modifier: Modifier = Modifier) {
    CartoonSurface(
        modifier = modifier.fillMaxWidth(),
        color = FeudColors.panelDark,
        borderWidth = 3.dp,
        corner = 14.dp,
        shadow = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun AnswerSlot(
    position: Int,
    answer: Answer,
    revealHiddenText: Boolean,
    showHiddenPoints: Boolean,
    enabled: Boolean,
    height: Dp?,
    modifier: Modifier = Modifier,
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
        modifier = modifier
            .fillMaxWidth()
            .then(if (height != null) Modifier.height(height) else Modifier)
            .graphicsLayer {
                // بس المكشوفة بتتحرك؛ المخفية ثابتة.
                if (revealed) {
                    rotationX = flip
                    cameraDistance = 14f * density
                }
            },
        color = if (revealed) FeudColors.team1 else FeudColors.cream,
        borderWidth = 3.dp,
        corner = 14.dp,
        shadow = 4.dp,
        onClick = if (enabled) onClick else null,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SlotNumber(position = position, revealed = revealed)
            Spacer(Modifier.width(8.dp))
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
                style = MaterialTheme.typography.titleSmall,
                // المضيف لازم يقرا الجواب كامل — سطرين بدل القص.
                maxLines = 2,
                softWrap = true,
                overflow = TextOverflow.Visible,
                modifier = Modifier.weight(1f)
            )
            // اللاعب ما بيشوف قيمة الخانة قبل ما تنكشف.
            if (revealed || showHiddenPoints) {
                Text(
                    answer.points.ar(),
                    color = if (revealed) FeudColors.cream else FeudColors.pink,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun SlotNumber(position: Int, revealed: Boolean) {
    Box(
        modifier = Modifier
            .size(26.dp)
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
