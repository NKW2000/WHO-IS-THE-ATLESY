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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudColors

/**
 * كتابة نص قصير بكيبورد التطبيق. كيبورد النظام بينقسم نصين بالوضع
 * الأفقي، فمنكتب بكيبوردنا بكل مكان بيحتاج كتابة.
 */
@Composable
fun NamePromptDialog(
    title: String,
    initial: String,
    maxLength: Int = 18,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(initial) }
    val blocker = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FeudColors.ink.copy(alpha = 0.86f))
            .clickable(interactionSource = blocker, indication = null) {}
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = FeudColors.gold, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(14.dp))
                CartoonSurface(
                    modifier = Modifier.weight(1f),
                    color = FeudColors.cream,
                    corner = 14.dp,
                    shadow = 6.dp
                ) {
                    Text(
                        value.ifBlank { "اكتب الاسم" },
                        color = if (value.isBlank()) {
                            FeudColors.ink.copy(alpha = 0.35f)
                        } else {
                            FeudColors.ink
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                SecondaryButton(
                    text = "إلغاء",
                    onClick = onDismiss,
                    accent = FeudColors.pink,
                    modifier = Modifier.width(140.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            NameKeypad(
                onKey = { key -> if (value.length < maxLength) value += key },
                onBackspace = { value = value.dropLast(1) },
                onDone = { if (value.isNotBlank()) onConfirm(value.trim()) },
                doneEnabled = value.isNotBlank(),
                doneText = "تمام"
            )
        }
    }
}
