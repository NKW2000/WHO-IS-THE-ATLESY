package com.feudparty.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.AnswerBoardColumns
import com.feudparty.app.ui.components.AwardBanner
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.QuestionCard
import com.feudparty.app.ui.components.ScoreHeader
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.StatusBanner
import com.feudparty.app.ui.components.StrikeFlash
import com.feudparty.app.ui.components.accentFor
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.components.inkColor
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.Answer
import com.feudparty.core.game.BuzzState
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Player
import com.feudparty.core.game.PlayerMark
import com.feudparty.core.game.Question
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState
import com.feudparty.core.game.buzzedTeam

/**
 * وضع التجربة على جهاز واحد: لوح المضيف عاليمين، وشاشات كل اللاعبين
 * عالشمال. كل مربّع لاعب هو نفس شاشة جهازه — بيومض لما يجي دوره، وبتضغط
 * عليه بدل ما تضغط على جهازه.
 */
@Composable
fun DemoScreen(
    state: GameState,
    onBuzz: (String) -> Unit,
    onCorrect: (Int) -> Unit,
    onWrong: () -> Unit,
    onNextRound: () -> Unit,
    onStartFastMoneyTimer: () -> Unit,
    onSubmitFastMoney: (Int?) -> Unit,
    onRevealFastMoney: () -> Unit,
    onEndGame: () -> Unit,
    onBack: () -> Unit
) {
    if (state.phase == RoundPhase.FAST_MONEY) {
        FastMoneyHostScreen(
            state = state,
            onStartTimer = onStartFastMoneyTimer,
            onSubmit = onSubmitFastMoney,
            onReveal = onRevealFastMoney,
            onEndGame = onEndGame
        )
        return
    }

    if (state.gameOver) {
        GameOverScreen(state = state, onBackHome = onBack)
        return
    }

    Box {
        StageBackground(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)) {
            Row(modifier = Modifier.fillMaxSize()) {
                // أجهزة اللاعبين
                Column(modifier = Modifier.width(300.dp).fillMaxHeight()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Pill(text = "أجهزة اللاعبين", color = FeudColors.gold)
                        SecondaryButton(
                            text = "خروج",
                            onClick = onBack,
                            accent = FeudColors.pink,
                            modifier = Modifier.width(110.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    val armed = state.armedPlayerIds()
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.players.forEach { player ->
                            PlayerDevice(
                                player = player,
                                mark = state.markFor(player.id),
                                canPress = player.id in armed,
                                onPress = { onBuzz(player.id) }
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                // لوح المضيف
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScoreHeader(state)
                    Spacer(Modifier.height(8.dp))
                    QuestionCard(
                        round = state.currentQuestionIndex + 1,
                        totalRounds = state.questions.size,
                        category = state.currentQuestion?.category,
                        question = state.currentQuestion?.text ?: "—"
                    )
                    Spacer(Modifier.height(8.dp))
                    StatusBanner(text = hostStatusText(state), accent = accentFor(state.activeTeam))
                    Spacer(Modifier.height(8.dp))
                    AwardBanner(state)

                    Box(modifier = Modifier.weight(1f)) {
                        AnswerBoardColumns(
                            answers = state.currentQuestion?.answers.orEmpty(),
                            revealHiddenText = true,
                            enabledSlots = state.canJudgeNow(),
                            slotHeight = 48.dp,
                            onSlotClick = onCorrect
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SecondaryButton(
                            text = "غلط ✕",
                            onClick = onWrong,
                            enabled = state.canJudgeNow(),
                            accent = FeudColors.strike,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(10.dp))
                        PrimaryButton(
                            text = if (state.isLastRound) "الجولة السريعة" else "الجولة الجاية",
                            onClick = onNextRound,
                            enabled = state.phase == RoundPhase.ROUND_END,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        StrikeFlash(strikes = state.strikes)
    }
}

/** مربّع = جهاز لاعب واحد، بنفس ألوان الشاشة الحقيقية. */
@Composable
private fun PlayerDevice(
    player: Player,
    mark: PlayerMark,
    canPress: Boolean,
    onPress: () -> Unit
) {
    val blink = rememberInfiniteTransition(label = "device${player.id}")
    val phase by blink.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink${player.id}"
    )

    val target = when (mark) {
        PlayerMark.ARMED -> if (phase > 0.5f) FeudColors.cream else FeudColors.ink
        PlayerMark.BUZZED -> FeudColors.team2
        PlayerMark.CORRECT -> FeudColors.team1
        PlayerMark.WRONG -> FeudColors.pink
        PlayerMark.IDLE -> FeudColors.panelDark
    }
    val background by animateColorAsState(
        targetValue = target,
        animationSpec = tween(if (mark == PlayerMark.ARMED) 90 else 200),
        label = "deviceColor${player.id}"
    )
    val foreground = if (background.luminance() > 0.45f) FeudColors.ink else FeudColors.cream

    CartoonSurface(
        modifier = Modifier.fillMaxWidth(),
        color = background,
        borderWidth = 4.dp,
        corner = 16.dp,
        shadow = 5.dp,
        onClick = if (canPress) onPress else null,
        enabled = canPress
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    player.name,
                    color = foreground,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(10.dp)
                ) {
                    Text("", color = Color.Transparent)
                }
                Text(
                    if (player.teamId == TeamId.TEAM_1) "أحمر" else "أزرق",
                    color = player.teamId.color(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                deviceLine(mark),
                color = foreground.copy(alpha = 0.9f),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun deviceLine(mark: PlayerMark): String = when (mark) {
    PlayerMark.ARMED -> "اضغط!"
    PlayerMark.BUZZED -> "ضغط — احكم"
    PlayerMark.CORRECT -> "صح ✔"
    PlayerMark.WRONG -> "غلط ✘"
    PlayerMark.IDLE -> "استنى"
}

private fun GameState.canJudgeNow(): Boolean = when (phase) {
    RoundPhase.FACE_OFF -> buzzedTeam() != null
    RoundPhase.FACE_OFF_SECOND, RoundPhase.PLAY, RoundPhase.STEAL -> true
    else -> false
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun DemoScreenPreview() {
    FeudPartyTheme {
        DemoScreen(
            state = GameState(
                questions = listOf(
                    Question(
                        "q1",
                        "اذكر شي بيعمله الناس أول ما يصحوا",
                        listOf(
                            Answer("يشيّكوا الموبايل", 40, revealed = true),
                            Answer("يشربوا قهوة", 30),
                            Answer("يغسلوا وجّهم", 20),
                            Answer("يصلّوا", 10)
                        ),
                        "عام"
                    )
                ),
                players = listOf(
                    Player("d0", "سامر", TeamId.TEAM_1),
                    Player("d1", "هناء", TeamId.TEAM_1),
                    Player("d2", "ليلى", TeamId.TEAM_2),
                    Player("d3", "زيد", TeamId.TEAM_2)
                ),
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "الأحمر", 120, connected = true),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الأزرق", 80, connected = true)
                ),
                phase = RoundPhase.PLAY,
                controllingTeam = TeamId.TEAM_1,
                turnPlayerId = "d1",
                buzzState = BuzzState.CLOSED,
                pot = 40,
                strikes = 1
            ),
            onBuzz = {},
            onCorrect = {},
            onWrong = {},
            onNextRound = {},
            onStartFastMoneyTimer = {},
            onSubmitFastMoney = {},
            onRevealFastMoney = {},
            onEndGame = {},
            onBack = {}
        )
    }
}
