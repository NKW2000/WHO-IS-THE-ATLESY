package com.feudparty.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.AnswerBoardColumns
import com.feudparty.app.ui.components.MiniScore
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SecondaryButton
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
import com.feudparty.core.game.other

/**
 * جهاز اللاعب. شاشتين بس:
 *
 * 1. **الزر** — لما يكون دورك بالمواجهة، الشاشة كلها زر واحد بيومض. ما في
 *    ولا عنصر تاني، فما بتغلط بالضغط حتى لو ما بتتطلع عالجهاز.
 * 2. **اللوح** — بعد ما تضغط (أو لما يجي دورك باللعب) بيبيّن السؤال
 *    والخانات الفاضية، وبتنكشف وحدة وحدة مع حكم المضيف.
 *
 * اللون بيضل هو الرسالة: أزرق ضغطت، أخضر صح، أحمر غلط.
 */
@Composable
fun PlayerScreen(
    state: GameState?,
    playerId: String?,
    teamId: TeamId?,
    mark: PlayerMark,
    status: ConnectionStatus,
    onBuzz: () -> Unit,
    onChoose: (Boolean) -> Unit = {}
) {
    val connected = status == ConnectionStatus.CONNECTED

    // الفائز بالمواجهة بيقرر من جهازه: نلعب أو نمرّر.
    if (connected &&
        state?.phase == RoundPhase.PLAY_OR_PASS &&
        playerId != null &&
        playerId in state.armedPlayerIds()
    ) {
        PlayOrPassScreen(state = state, teamId = teamId, onChoose = onChoose)
        return
    }

    val faceOffBuzzer = connected &&
        mark == PlayerMark.ARMED &&
        state?.phase == RoundPhase.FACE_OFF

    if (faceOffBuzzer) {
        val me = state?.player(playerId)
        FullScreenBuzzer(
            seat = me?.seat,
            opponent = me?.let { state.opponentOf(it)?.name },
            onBuzz = onBuzz
        )
    } else {
        PlayerBoard(
            state = state,
            playerId = playerId,
            teamId = teamId,
            mark = mark,
            status = status,
            onBuzz = onBuzz
        )
    }
}

/** قرار الفائز بالمواجهة — زرين كبار، بدون أي إشي تاني بالشاشة. */
@Composable
private fun PlayOrPassScreen(
    state: GameState,
    teamId: TeamId?,
    onChoose: (Boolean) -> Unit
) {
    val other = state.teams[teamId?.other()]?.name ?: "الفريق التاني"

    Box(modifier = Modifier.fillMaxSize().background(FeudColors.stage).padding(20.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "كسبتوا المواجهة!",
                color = FeudColors.gold,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "بدكم تلعبوا اللوح ولا تمرّروه لـ$other؟",
                color = FeudColors.text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                PrimaryButton(
                    text = "نلعب",
                    onClick = { onChoose(true) },
                    color = FeudColors.lime,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                SecondaryButton(
                    text = "نمرّرها",
                    onClick = { onChoose(false) },
                    accent = FeudColors.pink,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** الشاشة كلها زر — وميض كريمي/أسود، وأي لمسة بأي مكان بتحتسب. */
@Composable
private fun FullScreenBuzzer(
    seat: Int?,
    opponent: String?,
    onBuzz: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val blink = rememberInfiniteTransition(label = "blink")
    val phase by blink.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkPhase"
    )
    val pulse by blink.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(560), RepeatMode.Reverse),
        label = "pulse"
    )

    val background = if (phase > 0.5f) FeudColors.cream else FeudColors.ink
    val foreground = if (background.luminance() > 0.45f) FeudColors.ink else FeudColors.cream
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .clickable(
                interactionSource = interaction,
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onBuzz()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "اضغط!",
                color = foreground,
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(pulse)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                when {
                    seat != null && opponent != null -> "رقم ${seat.ar()} — وش لوش مع $opponent"
                    seat != null -> "رقم ${seat.ar()}"
                    else -> "الشاشة كلها زر"
                },
                color = foreground.copy(alpha = 0.75f),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/** اللوح: السؤال والخانات، ولون الحالة على كل الخلفية. */
@Composable
private fun PlayerBoard(
    state: GameState?,
    playerId: String?,
    teamId: TeamId?,
    mark: PlayerMark,
    status: ConnectionStatus,
    onBuzz: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val target = when (mark) {
        PlayerMark.BUZZED -> FeudColors.team2
        PlayerMark.CORRECT -> FeudColors.team1
        PlayerMark.WRONG -> FeudColors.pink
        else -> FeudColors.stage
    }
    val background by animateColorAsState(target, tween(220), label = "boardColor")
    val onBackground = if (background.luminance() > 0.45f) FeudColors.ink else FeudColors.cream

    // بمرحلة اللعب دورك بينبّه: أي لمسة بتقول للمضيف إنك عم تجاوب.
    val canSignal = mark == PlayerMark.ARMED
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .clickable(
                enabled = canSignal,
                interactionSource = interaction,
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onBuzz()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(state, playerId, teamId, status, onBackground)
            Spacer(Modifier.height(10.dp))

            val question = state?.currentQuestion
            val questionText = question?.text.orEmpty()
            Text(
                text = questionText.ifBlank { waitingLine(state, status) },
                color = onBackground,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            // اللوح بيبان للكل حتى وهو السؤال مخفي — خانات فاضية بتنكشف
            // وحدة وحدة مع حكم المضيف.
            if (question != null) {
                AnswerBoardColumns(
                    answers = question.answers,
                    revealHiddenText = false,
                    slotHeight = 46.dp,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Box(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state != null &&
                    (state.phase == RoundPhase.PLAY || state.phase == RoundPhase.STEAL)
                ) {
                    StrikeRow(strikes = state.strikes, size = 30.dp)
                    Spacer(Modifier.width(12.dp))
                }
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        statusLine(mark, state, teamId, status),
                        color = onBackground,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    state: GameState?,
    playerId: String?,
    teamId: TeamId?,
    status: ConnectionStatus,
    onBackground: Color
) {
    val me = state?.player(playerId)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (teamId != null && me != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SeatBadge(seat = me.seat, size = 26.dp)
                Spacer(Modifier.width(8.dp))
                Pill(
                    text = me.name,
                    color = teamId.color(),
                    textColor = teamId.inkColor()
                )
            }
        } else {
            Text(
                connectionLabel(status),
                color = onBackground,
                style = MaterialTheme.typography.titleSmall
            )
        }
        if (state != null) {
            Row(modifier = Modifier.width(300.dp)) {
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

private fun waitingLine(state: GameState?, status: ConnectionStatus): String = when {
    status != ConnectionStatus.CONNECTED -> connectionLabel(status)
    state == null -> "بانتظار المضيف"
    state.gameOver -> "انتهت اللعبة"
    else -> "استنى — المضيف عم يقرا السؤال"
}

private fun statusLine(
    mark: PlayerMark,
    state: GameState?,
    teamId: TeamId?,
    status: ConnectionStatus
): String {
    if (status != ConnectionStatus.CONNECTED) return connectionLabel(status)
    if (state == null) return "بانتظار المضيف"
    if (state.gameOver) return "انتهت اللعبة"

    return when (mark) {
        PlayerMark.ARMED -> when (state.phase) {
            RoundPhase.STEAL -> "دورك — جواب واحد بس، دوس لما تجاوب"
            else -> "دورك — جاوب، ودوس عالشاشة"
        }

        PlayerMark.BUZZED -> "ضغطت! المضيف عم يسمع جوابك"
        PlayerMark.CORRECT -> "صح ✔ — الدور بينتقل لزميلك"
        PlayerMark.WRONG -> "غلط ✘ — استنى لحد ما يخلّص زمايلك"
        PlayerMark.IDLE -> when {
            state.phase == RoundPhase.PLAY_OR_PASS ->
                "${state.teams[state.faceOffWinner]?.name ?: ""} عم يقرر يلعب أو يمرّر"

            state.phase == RoundPhase.ROUND_END && state.roundWinner == teamId -> "الجولة إلنا!"
            state.phase == RoundPhase.ROUND_END -> "انتهت الجولة"
            state.turnPlayerId != null ->
                state.player(state.turnPlayerId)?.name?.let { "الدور على $it" } ?: "استنى دورك"

            state.activeTeam == teamId -> "دور فريقك"
            else -> "استنى دورك"
        }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun PlayerBoardPreview() {
    FeudPartyTheme {
        PlayerScreen(
            state = GameState(
                questions = listOf(
                    Question(
                        "q1",
                        "اذكر شي بيعمله الناس أول ما يصحوا",
                        listOf(
                            Answer("يشيّكوا الموبايل", 40, revealed = true),
                            Answer("", 30),
                            Answer("", 20),
                            Answer("", 10)
                        ),
                        "عام"
                    )
                ),
                players = listOf(
                    Player("p1", "سامر", TeamId.TEAM_1, seat = 1),
                    Player("p2", "ليلى", TeamId.TEAM_2, seat = 2)
                ),
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "الأحمر", score = 120),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الأزرق", score = 80)
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
