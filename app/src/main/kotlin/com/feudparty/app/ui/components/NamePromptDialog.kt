package com.feudparty.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudShape

/**
 * كتابة اسم قصير بكيبورد الجهاز — خانة كريمية وزرّين تحتها. ما عاد في
 * كيبورد خاص بالتطبيق.
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
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FeudColors.ink.copy(alpha = 0.86f))
            .clickable(interactionSource = blocker, indication = null) {}
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        CartoonSurface(
            modifier = Modifier.fillMaxWidth(),
            color = FeudColors.stage,
            borderWidth = 4.dp,
            corner = FeudShape.block,
            shadow = 8.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(title, color = FeudColors.gold, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .blockSkin(FeudColors.cream)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = { if (it.length <= maxLength) value = it.trimStart() },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = FeudColors.ink
                        ),
                        cursorBrush = SolidColor(FeudColors.ink),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { if (value.isNotBlank()) onConfirm(value.trim()) }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focus),
                        decorationBox = { field ->
                            if (value.isEmpty()) {
                                Text(
                                    "اكتب الاسم",
                                    color = FeudColors.ink.copy(alpha = 0.35f),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                            field()
                        }
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(
                        text = "إلغاء",
                        onClick = onDismiss,
                        accent = FeudColors.pink,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "تمام",
                        onClick = { if (value.isNotBlank()) onConfirm(value.trim()) },
                        color = FeudColors.lime,
                        enabled = value.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
