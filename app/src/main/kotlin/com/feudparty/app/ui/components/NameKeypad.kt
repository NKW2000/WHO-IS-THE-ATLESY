package com.feudparty.app.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
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
/**
 * ترتيب كيبورد «عربي ١٠١» — نفس اللي بويندوز وبكيبورد أندرويد: كل حرف
 * بمكان زرّه على صفوف QWERTY، والصفوف بتنعرض من الشمال لليمين زي أي
 * كيبورد حقيقي (ض أول حرف عالشمال، مش عاليمين).
 *
 * [stagger] هو إزاحة أول الصف بعرض نص زر — زي إزاحة صفوف الكيبورد.
 */
private data class KeyRow(val letters: List<String>, val stagger: Float)

private val ROWS = listOf(
    KeyRow("ض ص ث ق ف غ ع ه خ ح ج د".split(" "), 0f),
    KeyRow("ش س ي ب ل ا ت ن م ك ط".split(" "), 0.5f),
    KeyRow("ئ ء ؤ ر لا ى ة و ز ظ".split(" "), 1f)
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
    // باقي التطبيق RTL، بس الكيبورد نفسه بينرسم من الشمال لليمين: هيك
    // بيوقع كل حرف بنفس مكانه على الكيبورد اللي متعوّد عليه المستخدم.
    // وبياخد كل الارتفاع المعطى له، فما بيضل فراغ ميت تحت.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
        val widest = ROWS.maxOf { it.letters.size }
        ROWS.forEach { row ->
            val trailing = widest - row.letters.size - row.stagger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (row.stagger > 0f) Spacer(Modifier.weight(row.stagger))
                row.letters.forEach { letter ->
                    Key(label = letter, modifier = Modifier.weight(1f)) { onKey(letter) }
                }
                if (trailing > 0f) Spacer(Modifier.weight(trailing))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Key(
                label = "مسح ⌫",
                color = FeudColors.pink,
                textColor = Color.White,
                modifier = Modifier.weight(2.5f),
                onClick = onBackspace
            )
            Key(label = "مسافة", modifier = Modifier.weight(5f)) { onKey(" ") }
            Key(
                label = doneText,
                color = if (doneEnabled) FeudColors.lime else FeudColors.panelDark,
                textColor = if (doneEnabled) FeudColors.ink else FeudColors.outlineSoft,
                enabled = doneEnabled,
                modifier = Modifier.weight(3.5f),
                onClick = onDone
            )
        }
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
        modifier = modifier.fillMaxHeight(),
        color = color,
        borderWidth = 3.dp,
        corner = 10.dp,
        shadow = 4.dp,
        onClick = onClick,
        enabled = enabled
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
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
fun KeypadSpacer() = Spacer(Modifier.height(46.dp).width(1.dp))
