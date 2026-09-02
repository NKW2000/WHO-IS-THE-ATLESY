package com.feudparty.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.theme.FeudColors
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

    StageBackground(contentPadding = PaddingValues(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("إعداد الاستضافة", color = FeudColors.gold, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "افتح التطبيق على جهاز كل فريق واختار «انضمام كفريق».",
                color = FeudColors.textMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(24.dp))

            SecondaryButton(
                text = if (advertising) "جاري البث..." else "بدء البث للفرق",
                onClick = onStartHosting,
                enabled = !advertising,
                modifier = Modifier.fillMaxWidth()
            )

            if (advertising && connectedCount < 2) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = FeudColors.gold
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("بانتظار الفرق...", color = FeudColors.textMuted)
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "الفرق المتصلة: $connectedCount / 2",
                color = FeudColors.text,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(10.dp))
            teams.forEach { team ->
                TeamStatusRow(team)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.weight(1f))
            PrimaryButton(
                text = "بدء اللعبة",
                onClick = onBeginGame,
                enabled = connectedCount == 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TeamStatusRow(team: TeamState) {
    val color = team.id.color()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FeudColors.panelDark, RoundedCornerShape(12.dp))
            .border(
                2.dp,
                if (team.connected) color else FeudColors.panel,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(if (team.connected) color else FeudColors.textMuted, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Text(team.name, color = FeudColors.text, style = MaterialTheme.typography.titleMedium)
        }
        Text(
            if (team.connected) "متصل" else "غير متصل",
            color = if (team.connected) color else FeudColors.textMuted,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview(showBackground = true, heightDp = 720)
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
