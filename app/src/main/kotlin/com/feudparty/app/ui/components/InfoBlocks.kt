package com.feudparty.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.ar
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudShape

/** بلوك الجولة — نفس الشكل عند المضيف وعند اللاعب. */
@Composable
fun RoundBlock(
    round: Int,
    totalRounds: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoBlock(
            label = "الجولة",
            value = "${round.ar()}/${totalRounds.ar()}",
            accent = FeudColors.teal,
            modifier = Modifier.weight(1f)
        )
    }
}

/** بلوك صغير: عنوان بلون مميّز وتحته القيمة. */
@Composable
fun InfoBlock(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    CartoonSurface(
        modifier = modifier,
        color = FeudColors.stageAlt,
        borderWidth = 3.dp,
        corner = FeudShape.block,
        shadow = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                color = accent,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
            Text(
                value,
                color = FeudColors.cream,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
