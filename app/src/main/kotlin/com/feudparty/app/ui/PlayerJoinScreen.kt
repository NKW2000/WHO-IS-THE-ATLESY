package com.feudparty.app.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.QuestionTile
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.theme.DisplayFont
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

/**
 * انضمام لاعب — خطوة وحدة بس: اكتب اسمك واضغط. الاسم بيتكتب بحقل كبير
 * كريمي عشان يبان من بعيد، والزر بيكبر لما يصير في اسم.
 */
@Composable
fun PlayerJoinScreen(onJoinConfirmed: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val ready = name.isNotBlank()

    StageBackground(contentPadding = PaddingValues(horizontal = 34.dp, vertical = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "شو اسمك؟",
                    color = FeudColors.gold,
                    style = MaterialTheme.typography.displaySmall
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "المضيف بيحطك بفريق، وجهازك بيصير زرّك.",
                    color = FeudColors.textMuted,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(18.dp))

                CartoonSurface(
                    modifier = Modifier.fillMaxWidth(),
                    color = FeudColors.cream,
                    corner = 18.dp,
                    shadow = 7.dp
                ) {
                    TextField(
                        value = name,
                        onValueChange = { if (it.length <= 16) name = it },
                        singleLine = true,
                        placeholder = {
                            Text(
                                "اكتب اسمك هون",
                                color = FeudColors.ink.copy(alpha = 0.35f),
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = DisplayFont,
                            color = FeudColors.ink
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { if (ready) onJoinConfirmed(name.trim()) }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = FeudColors.pink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(18.dp))

                PrimaryButton(
                    text = if (ready) "يلا نلعب" else "اكتب اسمك",
                    onClick = { onJoinConfirmed(name.trim()) },
                    enabled = ready,
                    color = FeudColors.lime,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.width(34.dp))

            Column(
                modifier = Modifier.width(260.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuestionTile(size = 150.dp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "خلّي البلوتوث والواي فاي شغالين",
                    color = FeudColors.textMuted,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
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
