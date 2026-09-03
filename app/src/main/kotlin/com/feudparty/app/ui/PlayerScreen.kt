package com.feudparty.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.BuzzerButton
import com.feudparty.app.ui.components.MiniScore
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.StrikeRow
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.components.inkColor
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.app.viewmodel.PlayerViewModel.ConnectionStatus
import com.feudparty.core.game.Answer
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Player
import com.feudparty.core.game.PlayerMark
import com.feudparty.core.game.Question
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState

/**
 * جهاز اللاعب — الشاشة كلها هي الزر، ولونها هو كل الرسالة:
 *
 * - **وميض كريمي/أسود**: دورك، اضغط.
 * - **أزرق**: ضغطت وصوتك وصل للمضيف.
 * - **أخضر**: جوابك صح.
 * - **أحمر**: جوابك غلط — وبيضل أحمر لحد ما يرجع دورك بعد ما يجاوبوا زمايلك.
 *
 * السؤال ما بيوصل هالجهاز أصلاً؛ اللاعب بيسمعه من المضيف.
 */
@Composable
fun PlayerScreen(
    state: GameState?,
    playerId: String?,
    teamId: TeamId?,
    mark: PlayerMark,
    status: ConnectionStatus,
    onBuzz: () -> Unit
) {
    val blink = rememberInfiniteTransition(label = "blink")
    val blinkPhase by blink.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkPhase"
    )

    val target = when (mark) {
        // الوميض بينط بين كريمي وأسود — أوضح إشي بغرفة فيها ٦ لاعبين.
        PlayerMark.ARMED -> if (blinkPhase > 0.5f) FeudColors.cream else FeudColors.ink
        PlayerMark.BUZZED -> FeudColors.team2
        PlayerMark.CORRECT -> FeudColors.team1
        PlayerMark.WRONG -> FeudColors.pink
        PlayerMark.IDLE -> FeudColors.stage
    }
    val background by animateColorAsState(
        targetValue = target,
        animationSpec = tween(if (mark == PlayerMark.ARMED) 90 else 220),
        label = "screenColor"
    )
    val onBackground = if (background.luminance() > 0.45f) FeudColors.ink else FeudColors.cream

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(18.dp)
    ) {
        PlayerTopBar(state, playerId, teamId, status, onBackground)

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    headline(mark, state, teamId, status),
                    color = onBackground,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center
                )
                val sub = subLine(mark, state, playerId)
                if (sub != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        sub,
                        color = onBackground.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
                if (state != null &&
                    (state.phase == RoundPhase.PLAY || state.phase == RoundPhase.STEAL)
                ) {
                    Spacer(Modifier.height(14.dp))
                    StrikeRow(strikes = state.strikes, size = 32.dp)
                }
            }

            BuzzerButton(
                label = buzzLabel(mark, state, status),
                subLabel = null,
                enabled = mark == PlayerMark.ARMED,
                accent = when (mark) {
                    PlayerMark.ARMED -> FeudColors.pink
                    PlayerMark.BUZZED -> FeudColors.gold
                    PlayerMark.CORRECT -> FeudColors.lime
                    PlayerMark.WRONG -> FeudColors.panelDark
                    PlayerMark.IDLE -> FeudColors.panelDark
                },
                onClick = onBuzz,
                size = 210.dp,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun PlayerTopBar(
    state: GameState?,
    playerId: String?,
    teamId: TeamId?,
    status: ConnectionStatus,
    onBackground: Color
) {
    val me = state?.player(playerId)
    val teamName = teamId?.let { state?.teams?.get(it)?.name }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (teamId != null && me != null) {
            Pill(
                text = "${me.name} — $teamName",
                color = teamId.color(),
                textColor = teamId.inkColor()
            )
        } else {
            Text(
                connectionLabel(status),
                color = onBackground.copy(alpha = 0.9f),
                style = MaterialTheme.typography.titleSmall
            )
        }
        if (state != null) {
            Row(modifier = Modifier.width(320.dp)) {
                MiniScore(state, TeamId.TEAM_1, Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                MiniScore(state, TeamId.TEAM_2, Modifier.weight(1f))
            }
        }
    }
}

private fun connectionLabel(status: ConnectionStatus): String = when (status) {
    ConnectionStatus.IDLE -> "غير متصل"
    ConnectionStatus.SEARCHING -> "جاري البحث عن المضيف..."
    ConnectionStatus.CONNECTED -> "متصل"
    ConnectionStatus.DISCONNECTED -> "انقطع الاتصال"
}

private fun buzzLabel(mark: PlayerMark, state: GameState?, status: ConnectionStatus): String {
    if (status != ConnectionStatus.CONNECTED || state == null) return "استنى"
    if (state.gameOver) return "انتهت"
    return when (mark) {
        PlayerMark.ARMED -> "جاوب!"
        PlayerMark.BUZZED -> "ضغطت!"
        PlayerMark.CORRECT -> "صح ✔"
        PlayerMark.WRONG -> "غلط ✘"
        PlayerMark.IDLE -> "استنى"
    }
}

private fun headline(
    mark: PlayerMark,
    state: GameState?,
    teamId: TeamId?,
    status: ConnectionStatus
): String {
    if (status != ConnectionStatus.CONNECTED) return connectionLabel(status)
    if (state == null) return "بانتظار المضيف"
    if (state.gameOver) return "انتهت اللعبة"

    return when (mark) {
        PlayerMark.ARMED -> if (state.phase == RoundPhase.FACE_OFF) "اضغط!" else "دورك"
        PlayerMark.BUZZED -> "ضغطت أول!"
        PlayerMark.CORRECT -> "صح ✔"
        PlayerMark.WRONG -> "غلط ✘"
        PlayerMark.IDLE -> when {
            state.phase == RoundPhase.ROUND_END && state.roundWinner == teamId -> "الجولة إلنا!"
            state.phase == RoundPhase.ROUND_END -> "انتهت الجولة"
            state.activeTeam == teamId -> "دور فريقك"
            else -> "استنى دورك"
        }
    }
}

private fun subLine(mark: PlayerMark, state: GameState?, playerId: String?): String? {
    if (state == null) return null
    return when (mark) {
        PlayerMark.ARMED -> when (state.phase) {
            RoundPhase.FACE_OFF -> "إنت عالمنصة — أول ضغطة بتجاوب"
            RoundPhase.STEAL -> "فرصة السرقة — جواب واحد بس"
            else -> "قول جوابك للمضيف"
        }

        PlayerMark.BUZZED -> "المضيف عم يسمع جوابك"
        PlayerMark.WRONG -> "استنى لحد ما يخلّص زمايلك دورهم"
        PlayerMark.CORRECT -> "الدور بينتقل لزميلك"
        PlayerMark.IDLE -> state.turnPlayerId
            ?.takeIf { it != playerId }
            ?.let { state.player(it)?.name }
            ?.let { "الدور على $it" }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun PlayerScreenPreview() {
    FeudPartyTheme {
        PlayerScreen(
            state = GameState(
                questions = listOf(Question("q1", "", listOf(Answer("", 40)), "عام")),
                players = listOf(
                    Player("p1", "سامر", TeamId.TEAM_1),
                    Player("p2", "ليلى", TeamId.TEAM_2)
                ),
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "النجوم", score = 120),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الصقور", score = 80)
                ),
                phase = RoundPhase.PLAY,
                controllingTeam = TeamId.TEAM_1,
                turnPlayerId = "p1",
                strikes = 2
            ),
            playerId = "p1",
            teamId = TeamId.TEAM_1,
            mark = PlayerMark.ARMED,
            status = ConnectionStatus.CONNECTED,
            onBuzz = {}
        )
    }
}
