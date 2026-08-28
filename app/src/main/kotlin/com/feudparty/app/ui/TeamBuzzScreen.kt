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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.app.ui.theme.TeamColors
import com.feudparty.app.viewmodel.TeamViewModel.ConnectionStatus
import com.feudparty.core.game.Answer
import com.feudparty.core.game.BuzzState
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState

/**
 * شاشة الفريق — الجهاز هون زر فقط: بيبعت البزّة وبيعرض الحالة اللي بتوصل
 * من المضيف. ما بيقرر ولا بيحسب إشي محلياً.
 */
@Composable
fun TeamBuzzScreen(
    state: GameState?,
    myTeam: TeamId?,
    status: ConnectionStatus,
    canBuzz: Boolean,
    onBuzz: () -> Unit
) {
    val lockedBy = state?.lockedTeam()
    val iAmLocked = lockedBy != null && lockedBy == myTeam

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatusHeader(state, myTeam, status)
        Spacer(Modifier.height(24.dp))

        Text(
            state?.currentQuestion?.text ?: "بانتظار السؤال من المضيف",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onBuzz,
            enabled = canBuzz,
            shape = CircleShape,
            modifier = Modifier.size(220.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = myTeam?.color() ?: MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                when {
                    iAmLocked -> "بزّيت!"
                    lockedBy != null -> "الفريق التاني بزّ"
                    state?.gameOver == true -> "انتهت"
                    canBuzz -> "بزّ!"
                    else -> "استنى"
                },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.weight(1f))
        ScoreRow(state)
    }
}

@Composable
private fun StatusHeader(state: GameState?, myTeam: TeamId?, status: ConnectionStatus) {
    val (text, color) = when (status) {
        ConnectionStatus.IDLE -> "غير متصل" to Color(0xFF9E9E9E)
        ConnectionStatus.SEARCHING -> "جاري البحث عن المضيف..." to Color(0xFFF5B841)
        ConnectionStatus.CONNECTED -> {
            val teamName = myTeam?.let { state?.teams?.get(it)?.name }
            (teamName?.let { "متصل — إنت $it" } ?: "متصل — بانتظار تخصيص الفريق") to Color(0xFF2E7D32)
        }
        ConnectionStatus.DISCONNECTED -> "انقطع الاتصال بالمضيف" to Color(0xFFC62828)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Text(
            text,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun ScoreRow(state: GameState?) {
    if (state == null) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(TeamId.TEAM_1, TeamId.TEAM_2).forEach { teamId ->
            val team = state.teams[teamId] ?: return@forEach
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(team.name, style = MaterialTheme.typography.labelLarge)
                Text("${team.score}", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.width(8.dp))
        }
    }
}

private fun GameState.lockedTeam(): TeamId? = when (buzzState) {
    BuzzState.LOCKED_TEAM_1 -> TeamId.TEAM_1
    BuzzState.LOCKED_TEAM_2 -> TeamId.TEAM_2
    else -> null
}

private fun TeamId.color(): Color = when (this) {
    TeamId.TEAM_1 -> TeamColors.team1
    TeamId.TEAM_2 -> TeamColors.team2
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun TeamBuzzScreenPreview() {
    FeudPartyTheme {
        TeamBuzzScreen(
            state = GameState(
                questions = listOf(
                    Question("q1", "اذكر مكان بيروح عليه الناس بالعطلة", listOf(Answer("البحر", 35)), "عام")
                ),
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "النجوم", score = 40, connected = true),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الصقور", connected = true)
                )
            ),
            myTeam = TeamId.TEAM_2,
            status = ConnectionStatus.CONNECTED,
            canBuzz = true,
            onBuzz = {}
        )
    }
}
