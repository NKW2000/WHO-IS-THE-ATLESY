package com.feudparty.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudColors

/**
 * سؤال تأكيد بستايل اللعبة. بيغطي الشاشة كلها وبيبلع اللمسات، فما بتنضغط
 * اللعبة اللي تحته بالغلط.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmColor: Color = FeudColors.pink
) {
    val blocker = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FeudColors.ink.copy(alpha = 0.82f))
            .clickable(interactionSource = blocker, indication = null) {},
        contentAlignment = Alignment.Center
    ) {
        CartoonSurface(
            modifier = Modifier.width(600.dp),
            color = FeudColors.stageAlt,
            corner = 22.dp,
            shadow = 9.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(22.dp)) {
                Text(title, color = FeudColors.gold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    message,
                    color = FeudColors.textSoft,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(18.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    SecondaryButton(
                        text = dismissText,
                        onClick = onDismiss,
                        accent = FeudColors.teal,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    PrimaryButton(
                        text = confirmText,
                        onClick = onConfirm,
                        color = confirmColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
