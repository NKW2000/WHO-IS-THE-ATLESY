package com.feudparty.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.flatShadow
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

/** أطول اسم مسموح — حتى يضل يبيّن كامل بلستة اللاعبين. */
private const val MAX_NAME = 14

/**
 * اسم اللاعب — خانة وحدة بتفتح كيبورد الجهاز نفسه (بدون كيبورد خاص
 * بالتطبيق)، وتحتها زر بيضوي لما تكتب اسم. نفس الشكل بالوضعين.
 */
@Composable
fun PlayerJoinScreen(onJoinConfirmed: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val ready = name.isNotBlank()
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }

    StageBackground(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically)
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
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    BasicTextField(
                        value = name,
                        onValueChange = { if (it.length <= MAX_NAME) name = it.trimStart() },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = FeudColors.ink
                        ),
                        cursorBrush = SolidColor(FeudColors.ink),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { if (ready) onJoinConfirmed(name.trim()) }
                        ),
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
                    onClick = { onJoinConfirmed(name.trim()) },
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

@Preview(showBackground = true, widthDp = 385, heightDp = 770)
@Composable
private fun PlayerJoinScreenPreview() {
    FeudPartyTheme { PlayerJoinScreen(onJoinConfirmed = {}) }
}
