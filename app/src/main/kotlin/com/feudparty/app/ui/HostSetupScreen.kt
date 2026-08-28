package com.feudparty.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState

@Composable
fun HostSetupScreen(
    teams: List<TeamState>,
    advertising: Boolean,
    onStartHosting: () -> Unit,
    onBeginGame: () -> Unit
) {
    val connectedCount = teams.count { it.connected }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("إعداد الاستضافة", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "افتح التطبيق على جهاز كل فريق واختار «انضمام كفريق».",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onStartHosting,
            enabled = !advertising,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (advertising) "جاري البث..." else "بدء البث للفرق")
        }

        if (advertising && connectedCount < 2) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text("بانتظار الفرق...")
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("الفرق المتصلة: $connectedCount / 2", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        teams.forEach { team -> TeamStatusRow(team) }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onBeginGame,
            enabled = connectedCount == 2,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("بدء اللعبة")
        }
    }
}

@Composable
private fun TeamStatusRow(team: TeamState) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = CircleShape,
                    color = if (team.connected) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
                ) {}
                Spacer(Modifier.width(12.dp))
                Text(team.name, style = MaterialTheme.typography.titleSmall)
            }
            Text(if (team.connected) "متصل" else "غير متصل")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HostSetupScreenPreview() {
    FeudPartyTheme {
        HostSetupScreen(
            teams = listOf(
                TeamState(TeamId.TEAM_1, "النجوم", connected = true),
                TeamState(TeamId.TEAM_2, "فريق ٢")
            ),
            advertising = true,
            onStartHosting = {},
            onBeginGame = {}
        )
    }
}
