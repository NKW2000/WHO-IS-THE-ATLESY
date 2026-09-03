package com.feudparty.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.ar
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.GoldDivider
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.components.inkColor
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.Player
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState

/**
 * شاشة إعداد المضيف — كل جهاز بينضم بيصير لاعب، والمضيف بيشوف الفرق وهي
 * بتتعبّى. ترتيب اللاعبين بالقائمة هو ترتيب الدور بالجولة.
 */
@Composable
fun HostSetupScreen(
    teams: Map<TeamId, TeamState>,
    players: List<Player>,
    advertising: Boolean,
    minPerTeam: Int,
    onStartHosting: () -> Unit,
    onBeginGame: () -> Unit
) {
    val readyTeams = TeamId.entries.count { team ->
        players.count { it.teamId == team && it.connected } >= minPerTeam
    }

    StageBackground(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "إعداد اللعبة",
                        color = FeudColors.gold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "كل لاعب بيفتح التطبيق على جهازه ويختار «انضمام كلاعب».",
                        color = FeudColors.textMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                if (advertising && readyTeams < 2) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = FeudColors.gold
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("بانتظار اللاعبين...", color = FeudColors.textMuted)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            GoldDivider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.weight(1f)) {
                TeamId.entries.forEachIndexed { index, teamId ->
                    if (index > 0) Spacer(Modifier.width(14.dp))
                    TeamColumn(
                        team = teams[teamId],
                        teamId = teamId,
                        players = players.filter { it.teamId == teamId },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SecondaryButton(
                    text = if (advertising) "البث شغّال" else "بدء البث للاعبين",
                    onClick = onStartHosting,
                    enabled = !advertising,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                PrimaryButton(
                    text = "بدء اللعبة",
                    onClick = onBeginGame,
                    enabled = readyTeams == 2,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TeamColumn(
    team: TeamState?,
    teamId: TeamId,
    players: List<Player>,
    modifier: Modifier = Modifier
) {
    val color = teamId.color()
    val filled = players.any { it.connected }
    CartoonSurface(
        modifier = modifier.fillMaxHeight(),
        color = if (filled) color else FeudColors.ink.copy(alpha = 0.35f),
        corner = 22.dp,
        shadow = 8.dp
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                team?.name ?: "فريق",
                color = if (filled) teamId.inkColor() else color,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "${players.count { it.connected }.ar()} لاعب",
                color = if (filled) teamId.inkColor().copy(alpha = 0.8f) else FeudColors.textMuted,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.height(8.dp))

        if (players.isEmpty()) {
            Text(
                "بانتظار الاتصال...",
                color = FeudColors.textMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            return@Column
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            players.forEachIndexed { index, player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (player.connected) FeudColors.cream else FeudColors.textMuted,
                                CircleShape
                            )
                            .border(2.dp, FeudColors.ink, CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "${(index + 1).ar()}. ${player.name}",
                        color = when {
                            !player.connected -> FeudColors.textMuted
                            filled -> teamId.inkColor()
                            else -> FeudColors.text
                        },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (index == 0) {
                        Text(
                            "المنصة",
                            color = if (filled) teamId.inkColor() else FeudColors.gold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun HostSetupScreenPreview() {
    FeudPartyTheme {
        HostSetupScreen(
            teams = mapOf(
                TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "الفريق الأحمر", connected = true),
                TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الفريق الأزرق", connected = true)
            ),
            players = listOf(
                Player("a1", "سامر", TeamId.TEAM_1),
                Player("a2", "هناء", TeamId.TEAM_1),
                Player("b1", "ليلى", TeamId.TEAM_2)
            ),
            advertising = true,
            minPerTeam = 1,
            onStartHosting = {},
            onBeginGame = {}
        )
    }
}
