package com.feudparty.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.components.inkColor
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.Player
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.other
import com.feudparty.core.game.TeamState

/**
 * لوبي المضيف. كل إشي بيدخل بشاشة وحدة بدون تمرير: عمودين للفريقين،
 * وسطر واحد فوق وسطر أزرار تحت.
 */
@Composable
fun HostSetupScreen(
    teams: Map<TeamId, TeamState>,
    players: List<Player>,
    advertising: Boolean,
    minPerTeam: Int,
    onStartHosting: () -> Unit,
    onBeginGame: () -> Unit,
    onMovePlayer: (String, TeamId) -> Unit = { _, _ -> }
) {
    val ready = TeamId.entries.all { team ->
        players.count { it.teamId == team && it.connected } >= minPerTeam
    }

    StageBackground(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "مين معنا؟",
                        color = FeudColors.gold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "كل لاعب بيفتح التطبيق ويختار «انضمام كلاعب»",
                        color = FeudColors.textMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (advertising) BroadcastBadge() else Spacer(Modifier.width(1.dp))
            }

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.weight(1f)) {
                TeamId.entries.forEachIndexed { index, teamId ->
                    if (index > 0) Spacer(Modifier.width(12.dp))
                    TeamColumn(
                        team = teams[teamId],
                        teamId = teamId,
                        players = players.filter { it.teamId == teamId },
                        onMovePlayer = onMovePlayer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SecondaryButton(
                    text = if (advertising) "البث شغّال" else "بدء البث",
                    onClick = onStartHosting,
                    enabled = !advertising,
                    modifier = Modifier.width(240.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    if (ready) "جاهزين — يلا نبلّش" else "بدنا لاعب بكل فريق عالأقل",
                    color = if (ready) FeudColors.lime else FeudColors.textMuted,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                PrimaryButton(
                    text = "ابدأ اللعبة",
                    onClick = onBeginGame,
                    enabled = ready,
                    color = FeudColors.lime,
                    modifier = Modifier.width(240.dp)
                )
            }
        }
    }
}

/** نقطة بتنبض بتقول إنه البث شغّال. */
@Composable
private fun BroadcastBadge() {
    val transition = rememberInfiniteTransition(label = "broadcast")
    val pulse by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(pulse)
                .background(FeudColors.lime, CircleShape)
                .border(2.dp, FeudColors.ink, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Pill(text = "بانتظار اللاعبين", color = FeudColors.gold)
    }
}

@Composable
private fun TeamColumn(
    team: TeamState?,
    teamId: TeamId,
    players: List<Player>,
    onMovePlayer: (String, TeamId) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = teamId.color()
    val joined = players.count { it.connected }

    CartoonSurface(
        modifier = modifier.fillMaxHeight(),
        color = if (joined > 0) color else FeudColors.ink.copy(alpha = 0.35f),
        corner = 20.dp,
        shadow = 7.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    team?.name ?: "فريق",
                    color = if (joined > 0) teamId.inkColor() else color,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "${joined.ar()} لاعب",
                    color = if (joined > 0) {
                        teamId.inkColor().copy(alpha = 0.8f)
                    } else {
                        FeudColors.textMuted
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(Modifier.height(8.dp))

            if (players.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                    Text(
                        "بانتظار الاتصال...",
                        color = FeudColors.textMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                return@Column
            }

            // قائمة كسولة — تضل داخل الشاشة مهما زاد العدد.
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(players) { player ->
                    PlayerRow(
                        player = player,
                        teamId = teamId,
                        onMove = { onMovePlayer(player.id, teamId.other()) }
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun PlayerRow(player: Player, teamId: TeamId, onMove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // رقم اللاعب — هو نفسه رقم خصمه بالفريق التاني.
        SeatBadge(seat = player.seat, dim = !player.connected)
        Spacer(Modifier.width(10.dp))
        Text(
            player.name,
            color = if (player.connected) teamId.inkColor() else FeudColors.textMuted,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        // نقل اللاعب للفريق التاني قبل ما تبلّش اللعبة.
        CartoonSurface(
            color = FeudColors.gold,
            borderWidth = 3.dp,
            corner = 10.dp,
            shadow = 3.dp,
            onClick = onMove
        ) {
            Text(
                "بدّل ⇄",
                color = FeudColors.ink,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

/** رقم اللاعب بمربّع — نفس الرقم عند الخصم. */
@Composable
fun SeatBadge(seat: Int, dim: Boolean = false, size: Dp = 30.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                if (dim) FeudColors.textMuted else FeudColors.gold,
                RoundedCornerShape(9.dp)
            )
            .border(3.dp, FeudColors.ink, RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(seat.ar(), color = FeudColors.ink, style = MaterialTheme.typography.labelMedium)
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
                Player("a1", "سامر", TeamId.TEAM_1, seat = 1),
                Player("a2", "هناء", TeamId.TEAM_1, seat = 2),
                Player("b1", "ليلى", TeamId.TEAM_2, seat = 1)
            ),
            advertising = true,
            minPerTeam = 1,
            onStartHosting = {},
            onBeginGame = {}
        )
    }
}
