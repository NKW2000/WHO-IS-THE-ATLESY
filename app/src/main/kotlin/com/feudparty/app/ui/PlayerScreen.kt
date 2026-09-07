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
import com.feudparty.app.ui.components.AnswerBoardGrid
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.Countdown
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StrikeFlash
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
    onChoose: (Boolean) -> Unit = {},
    onChangeTeam: (TeamId) -> Unit = {}
) {
    val connected = status == ConnectionStatus.CONNECTED

    // قبل ما يبلّش المضيف: اللاعب بيشوف رقمه وفريقه، وبيقدر يبدّل فريق.
    if (connected && state != null && !state.matchStarted && !state.gameOver) {
        PlayerLobbyScreen(
            state = state,
            playerId = playerId,
            teamId = teamId,
            onChangeTeam = onChangeTeam
        )
        return
    }

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
        Box {
            PlayerBoard(
                state = state,
                playerId = playerId,
                teamId = teamId,
                mark = mark,
                status = status,
                onBuzz = onBuzz
            )
            // نفس حركة الخطأ اللي بتطلع عند المضيف — بتطلع عند الكل،
            // وكمان لما يخلص الوقت بدون جواب.
            StrikeFlash(strikes = state?.strikes ?: 0)
        }
    }
}

/**
 * لوبي اللاعب: اسمه ورقمه وفريقه، ومين معه بالفريق، وزر يبدّل فيه فريقه
 * قبل ما تبلّش اللعبة.
 */
@Composable
private fun PlayerLobbyScreen(
    state: GameState,
    playerId: String?,
    teamId: TeamId?,
    onChangeTeam: (TeamId) -> Unit
) {
    val me = state.player(playerId)
    val other = teamId?.other()

    Box(modifier = Modifier.fillMaxSize().background(FeudColors.stage).padding(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (me != null) {
                    SeatBadge(seat = me.seat, size = 34.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        me.name,
                        color = FeudColors.cream,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "بانتظار المضيف يبلّش",
                    color = FeudColors.gold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(14.dp))

            // الفرق بأسماء المضيف: دوس على فريق حتى تفوت فيه.
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                TeamId.entries.forEachIndexed { index, id ->
                    if (index > 0) Spacer(Modifier.width(12.dp))
                    TeamRoster(
                        state = state,
                        teamId = id,
                        mine = id == teamId,
                        onClick = { if (id != teamId) onChangeTeam(id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                if (other == null) "" else "دوس على الفريق التاني إذا بدك تبدّل",
                color = FeudColors.textMuted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** أسماء فريق مرتّبة برقم كل لاعب. */
@Composable
private fun TeamRoster(
    state: GameState,
    teamId: TeamId,
    mine: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val team = state.teams[teamId]
    CartoonSurface(
        modifier = modifier,
        color = if (mine) teamId.color() else FeudColors.stageAlt,
        corner = 18.dp,
        shadow = 7.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    team?.name ?: "فريق",
                    color = if (mine) teamId.inkColor() else FeudColors.textSoft,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (mine) {
                    Text(
                        "فريقك",
                        color = teamId.inkColor(),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            state.playersOf(teamId).forEach { player ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    SeatBadge(seat = player.seat, size = 24.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        player.name,
                        color = if (mine) teamId.inkColor() else FeudColors.cream,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1
                    )
                }
            }
            if (state.playersOf(teamId).isEmpty()) {
                Text(
                    "لسا ما فات حدا",
                    color = FeudColors.textMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
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
            if (state.choiceSecondsLeft > 0) {
                Countdown(seconds = state.choiceSecondsLeft)
                Spacer(Modifier.height(10.dp))
            }
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
            // فوق بالنص: الأخطاء والوقت — الشغلتين اللي لازم يشوفهن اللاعب
            // بلمحة عين وهو بيسمع السؤال.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state != null &&
                    (state.phase == RoundPhase.PLAY || state.phase == RoundPhase.STEAL)
                ) {
                    StrikeRow(strikes = state.strikes, total = state.strikesToSteal, size = 30.dp)
                }
                val seconds = state?.let { maxOf(it.answerSecondsLeft, it.choiceSecondsLeft) } ?: 0
                if (seconds > 0) {
                    Spacer(Modifier.width(14.dp))
                    Countdown(seconds = seconds, size = 40.dp)
                }
            }

            val question = state?.currentQuestion
            val questionText = question?.text.orEmpty()
            if (questionText.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = questionText,
                    color = onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // اللوح بيبان للكل حتى وهو السؤال مخفي: رقم الخانة بس، بدون
            // نص وبدون نقاط، لحد ما المضيف يكشفها.
            if (question != null) {
                AnswerBoardGrid(
                    answers = question.answers,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 6.dp),
                    revealHiddenText = false,
                    showHiddenPoints = false
                )
            } else {
                Box(modifier = Modifier.weight(1f))
            }

            // تحت: بلوك واحد بيقول مين عم يلعب برقمه، وشو المطلوب منّك.
            // نقاط الفرق مش هون — بتبيّن بشاشة النتيجة بين الجولات.
            TurnBlock(
                state = state,
                playerId = playerId,
                teamId = teamId,
                mark = mark,
                status = status
            )
        }
    }
}

/** بلوك الدور: رقم اللاعب اللي عليه الدور واسمه، وتحته سطر الحالة. */
@Composable
private fun TurnBlock(
    state: GameState?,
    playerId: String?,
    teamId: TeamId?,
    mark: PlayerMark,
    status: ConnectionStatus
) {
    val me = state?.player(playerId)
    val current = state?.player(state.turnPlayerId ?: state.buzzedPlayerId) ?: me
    val color = (current?.teamId ?: teamId)?.color() ?: FeudColors.gold
    val ink = (current?.teamId ?: teamId)?.inkColor() ?: FeudColors.ink

    CartoonSurface(
        modifier = Modifier.fillMaxWidth(),
        color = color,
        borderWidth = 3.dp,
        corner = 14.dp,
        shadow = 5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (current != null) {
                SeatBadge(seat = current.seat, size = 28.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    if (current.id == playerId) "دورك" else "دور ${current.name}",
                    color = ink,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Spacer(Modifier.width(12.dp))
            }
            Text(
                statusLine(mark, state, teamId, status),
                color = ink,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.End,
                maxLines = 2,
                modifier = Modifier.weight(1f)
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
