package com.feudparty.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.flatShadow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.unit.Dp
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

    if (isPortrait()) {
        PortraitJoin(
            name = name,
            onNameChange = { if (it.length <= 14) name = it },
            ready = ready,
            onConfirm = { if (ready) onJoinConfirmed(name.trim()) }
        )
        return
    }

    StageBackground(contentPadding = stagePadding()) {
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

/**
 * شاشة الاسم بالوضع الطولي — نفس التصميم: سؤال كبير، خانة كريمية بتفتح
 * كيبورد الجهاز، وزر تحت بيضوي لما تكتب اسمك.
 */
@Composable
private fun PortraitJoin(
    name: String,
    onNameChange: (String) -> Unit,
    ready: Boolean,
    onConfirm: () -> Unit
) {
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }

    StageBackground(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
            ) {
                Text(
                    "شو اسمك؟",
                    color = FeudColors.gold,
                    style = MaterialTheme.typography.displaySmall
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .flatShadow(7.dp, FeudColors.creamShadow, 18.dp)
                        .background(FeudColors.cream, RoundedCornerShape(18.dp))
                        .padding(20.dp)
                ) {
                    BasicTextField(
                        value = name,
                        onValueChange = onNameChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = FeudColors.ink
                        ),
                        cursorBrush = SolidColor(FeudColors.ink),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focus),
                        decorationBox = { field ->
                            if (name.isEmpty()) {
                                Text(
                                    "اكتب اسمك",
                                    color = FeudColors.ink.copy(alpha = 0.35f),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                            field()
                        }
                    )
                }

                Text(
                    "بعد ما تفوت بتختار فريقك من اللوبي",
                    color = FeudColors.textMuted,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (ready) {
                PrimaryButton(
                    text = "يلا نلعب",
                    onClick = onConfirm,
                    color = FeudColors.lime,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .flatShadow(7.dp, FeudColors.canvas, 18.dp)
                        .background(FeudColors.panelDark, RoundedCornerShape(18.dp))
                        .border(3.dp, FeudColors.stageAlt, RoundedCornerShape(18.dp))
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "اكتب اسمك",
                        color = FeudColors.outlineSoft,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
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
