package com.feudparty.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.NameKeypad
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

/**
 * انضمام لاعب: الاسم بس. الفرق بتبيّن بأسماء المضيف بعد ما يتصل — قبل
 * الاتصال ما منعرف شو سمّاهم، فما منخمّن.
 *
 * الاسم بينكتب بكيبورد التطبيق نفسه: كيبورد النظام بينقسم نصين بالوضع
 * الأفقي وهاد إعداد بالكيبورد مش بإيدنا.
 */
@Composable
fun PlayerJoinScreen(onJoinConfirmed: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val ready = name.isNotBlank()

    StageBackground(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "شو اسمك؟",
                    color = FeudColors.gold,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.width(14.dp))
                CartoonSurface(
                    modifier = Modifier.weight(1f),
                    color = FeudColors.cream,
                    corner = 14.dp,
                    shadow = 6.dp
                ) {
                    Text(
                        name.ifBlank { "اكتب اسمك" },
                        color = if (name.isBlank()) {
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
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "بعد ما تفوت بتختار فريقك من اللوبي",
                color = FeudColors.textMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(10.dp))

            NameKeypad(
                modifier = Modifier.weight(1f),
                onKey = { key -> if (name.length < 14) name += key },
                onBackspace = { name = name.dropLast(1) },
                onDone = { if (ready) onJoinConfirmed(name.trim()) },
                doneEnabled = ready,
                doneText = if (ready) "يلا نلعب" else "اكتب اسمك"
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun PlayerJoinScreenPreview() {
    FeudPartyTheme {
        Box { PlayerJoinScreen(onJoinConfirmed = {}) }
    }
}
