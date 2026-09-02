package com.feudparty.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

@Composable
fun PlayerJoinScreen(onJoinConfirmed: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    StageBackground(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "انضمام كلاعب",
                color = FeudColors.gold,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "اكتب اسمك — المضيف بيحطك بفريق، وجهازك بيصير زرّك. " +
                    "خلّي البلوتوث والواي فاي شغالين.",
                color = FeudColors.textMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 20) name = it },
                    label = { Text("اسمك") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FeudColors.gold,
                        unfocusedBorderColor = FeudColors.goldDim,
                        focusedLabelColor = FeudColors.gold,
                        unfocusedLabelColor = FeudColors.textMuted,
                        focusedTextColor = FeudColors.text,
                        unfocusedTextColor = FeudColors.text,
                        cursorColor = FeudColors.gold
                    )
                )
                Spacer(Modifier.width(14.dp))
                PrimaryButton(
                    text = "انضم",
                    onClick = { onJoinConfirmed(name.trim()) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.width(180.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun PlayerJoinScreenPreview() {
    FeudPartyTheme { PlayerJoinScreen(onJoinConfirmed = {}) }
}
