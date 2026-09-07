package com.feudparty.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudColors

/**
 * كيبورد عربي جوّا التطبيق.
 *
 * كيبورد النظام بينقسم نصين بالوضع الأفقي (إعداد بالكيبورد نفسه، مش
 * بإيدنا)، ولإدخال اسم قصير ما منحتاجه أصلاً — فمنعرض حروفنا وخلص.
 */
private val ROWS = listOf(
    "ض ص ث ق ف غ ع ه خ ح".split(" "),
    "ش س ي ب ل ا ت ن م ك".split(" "),
    "ظ ط ذ د ز ر و ة ى ء".split(" "),
    "ج".split(" ")
)

@Composable
fun NameKeypad(
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onDone: () -> Unit,
    doneEnabled: Boolean,
    doneText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ROWS.take(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { letter ->
                    Key(label = letter, modifier = Modifier.weight(1f)) { onKey(letter) }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Key(label = "ج", modifier = Modifier.weight(1f)) { onKey("ج") }
            Key(label = "مسافة", modifier = Modifier.weight(3f)) { onKey(" ") }
            Key(
                label = "مسح ⌫",
                color = FeudColors.pink,
                textColor = Color.White,
                modifier = Modifier.weight(2f),
                onClick = onBackspace
            )
            Key(
                label = doneText,
                color = if (doneEnabled) FeudColors.lime else FeudColors.panelDark,
                textColor = if (doneEnabled) FeudColors.ink else FeudColors.outlineSoft,
                enabled = doneEnabled,
                modifier = Modifier.weight(3f),
                onClick = onDone
            )
        }
    }
}

@Composable
private fun Key(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = FeudColors.cream,
    textColor: Color = FeudColors.ink,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    CartoonSurface(
        modifier = modifier,
        color = color,
        borderWidth = 3.dp,
        corner = 10.dp,
        shadow = 4.dp,
        onClick = onClick,
        enabled = enabled
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

/** فراغ بنفس ارتفاع صف مفاتيح — بيستعمل بالمعاينات. */
@Composable
fun KeypadSpacer() = Spacer(Modifier.height(52.dp).width(1.dp))
