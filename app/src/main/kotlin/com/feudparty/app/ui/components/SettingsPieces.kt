package com.feudparty.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.ar
import com.feudparty.app.ui.theme.FeudColors

/** قطع شاشات الإعدادات: بطاقة، عدّاد زائد/ناقص، وشريحة مضاعف الجولة. */
@Composable
fun SettingsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CartoonSurface(
        modifier = modifier.fillMaxWidth(),
        color = FeudColors.stageAlt,
        corner = 18.dp,
        shadow = 6.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = FeudColors.gold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun Stepper(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    step: Int = 1,
    onChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            color = FeudColors.text,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(10.dp))
        StepperButton(text = "−", enabled = value > min) {
            onChange((value - step).coerceAtLeast(min))
        }
        Box(
            modifier = Modifier.width(62.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value.ar(),
                color = FeudColors.gold,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
        StepperButton(text = "+", enabled = value < max) {
            onChange((value + step).coerceAtMost(max))
        }
    }
}

@Composable
private fun StepperButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    CartoonSurface(
        color = if (enabled) FeudColors.gold else FeudColors.panelDark,
        borderWidth = 3.dp,
        corner = 12.dp,
        shadow = 4.dp,
        onClick = onClick,
        enabled = enabled
    ) {
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            Text(
                text,
                color = if (enabled) FeudColors.ink else FeudColors.outlineSoft,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun MultiplierChip(
    round: Int,
    multiplier: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CartoonSurface(
        modifier = modifier,
        color = when (multiplier) {
            1 -> FeudColors.stage
            2 -> FeudColors.teal
            3 -> FeudColors.lime
            else -> FeudColors.pink
        },
        borderWidth = 3.dp,
        corner = 12.dp,
        shadow = 4.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "جولة ${round.ar()}",
                color = if (multiplier == 1) FeudColors.textMuted else FeudColors.ink,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                "×${multiplier.ar()}",
                color = if (multiplier == 1) FeudColors.text else FeudColors.ink,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
