package com.feudparty.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudPartyTheme

@Composable
fun TeamJoinScreen(onJoinConfirmed: (String) -> Unit) {
    var teamName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("انضمام كفريق", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "تأكد إنو المضيف بلّش البث، وإنو البلوتوث والواي فاي شغالين.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = teamName,
            onValueChange = { if (it.length <= 20) teamName = it },
            label = { Text("اسم الفريق") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onJoinConfirmed(teamName.trim()) },
            enabled = teamName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("انضم")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamJoinScreenPreview() {
    FeudPartyTheme { TeamJoinScreen(onJoinConfirmed = {}) }
}
