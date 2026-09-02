package com.feudparty.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

@Composable
fun TeamJoinScreen(onJoinConfirmed: (String) -> Unit) {
    var teamName by remember { mutableStateOf("") }

    StageBackground(contentPadding = PaddingValues(24.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("انضمام كفريق", color = FeudColors.gold, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "تأكد إنو المضيف بلّش البث، وإنو البلوتوث والواي فاي شغالين.",
                color = FeudColors.textMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(
                value = teamName,
                onValueChange = { if (it.length <= 20) teamName = it },
                label = { Text("اسم الفريق") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
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
            Spacer(Modifier.height(28.dp))
            PrimaryButton(
                text = "انضم",
                onClick = { onJoinConfirmed(teamName.trim()) },
                enabled = teamName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun TeamJoinScreenPreview() {
    FeudPartyTheme { TeamJoinScreen(onJoinConfirmed = {}) }
}
