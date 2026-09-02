package com.feudparty.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.StrikeRow
import com.feudparty.app.ui.components.color
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
 * - **وميض أبيض/أسود**: دورك، اضغط.
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
            animation = tween(260, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkPhase"
    )
    val glow by blink.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse),
        label = "glow"
    )

    val target = when (mark) {
        // الوميض بينط بين أبيض وأسود — أوضح إشي بغرفة فيها ٦ لاعبين.
        PlayerMark.ARMED -> if (blinkPhase > 0.5f) Color.White else Color(0xFF04060C)
        PlayerMark.BUZZED -> Color(0xFF1668FF).copy(alpha = glow).compositeOn(Color(0xFF04060C))
        PlayerMark.CORRECT -> Color(0xFF17B65A)
        PlayerMark.WRONG -> Color(0xFFD92121)
        PlayerMark.IDLE -> FeudColors.deepNavy
    }
    val background by animateColorAsState(
        targetValue = target,
        animationSpec = tween(if (mark == PlayerMark.ARMED) 90 else 220),
        label = "screenColor"
    )
    val onBackground = if (background.luminance() > 0.45f) Color(0xFF04060C) else Color.White
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .clickable(
                enabled = mark == PlayerMark.ARMED,
                interactionSource = interaction,
                indication = null,
                onClick = onBuzz
            )
            .padding(20.dp)
    ) {
        PlayerTopBar(state, playerId, teamId, status, onBackground)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                headline(mark, state, teamId, status),
                color = onBackground,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )
            val sub = subLine(mark, state, playerId)
            if (sub != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    sub,
                    color = onBackground.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (state != null && (state.phase == RoundPhase.PLAY || state.phase == RoundPhase.STEAL)) {
            StrikeRow(
                strikes = state.strikes,
                size = 30.dp,
                modifier = Modifier.align(Alignment.BottomCenter)
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (teamId != null) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(14.dp)
                        .background(teamId.color(), RoundedCornerShape(4.dp))
                        .border(1.dp, onBackground.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                listOfNotNull(me?.name, teamName).joinToString(" — ").ifBlank {
                    connectionLabel(status)
                },
                color = onBackground.copy(alpha = 0.9f),
                style = MaterialTheme.typography.titleMedium
            )
        }
        if (state != null) {
            Text(
                "${state.teams[TeamId.TEAM_1]?.score ?: 0} : ${state.teams[TeamId.TEAM_2]?.score ?: 0}",
                color = onBackground.copy(alpha = 0.75f),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private fun connectionLabel(status: ConnectionStatus): String = when (status) {
    ConnectionStatus.IDLE -> "غير متصل"
    ConnectionStatus.SEARCHING -> "جاري البحث عن المضيف..."
    ConnectionStatus.CONNECTED -> "متصل"
    ConnectionStatus.DISCONNECTED -> "انقطع الاتصال"
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
        PlayerMark.ARMED -> if (state.phase == RoundPhase.FACE_OFF) "اضغط!" else "دورك — جاوب"
        PlayerMark.BUZZED -> "ضغطت!"
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
        PlayerMark.CORRECT -> "جواب صح — الدور بينتقل لزميلك"
        PlayerMark.IDLE -> state.turnPlayerId
            ?.takeIf { it != playerId }
            ?.let { state.player(it)?.name }
            ?.let { "الدور على $it" }
    }
}

/** لون فوق لون — منستعمله للأزرق حتى يضل قوي بدون شفافية على شاشة سودا. */
private fun Color.compositeOn(background: Color): Color {
    val a = alpha
    return Color(
        red = red * a + background.red * (1 - a),
        green = green * a + background.green * (1 - a),
        blue = blue * a + background.blue * (1 - a),
        alpha = 1f
    )
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
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
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "الفريق الأحمر", score = 120),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الفريق الأزرق", score = 80)
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
