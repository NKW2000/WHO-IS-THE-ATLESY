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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.AnswerBoardColumns
import com.feudparty.app.ui.components.AwardBanner
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.QuestionCard
import com.feudparty.app.ui.components.ScoreHeader
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.StatusBanner
import com.feudparty.app.ui.components.StrikeFlash
import com.feudparty.app.ui.components.accentFor
import com.feudparty.app.ui.components.color
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
 * لوحة المضيف (أفقية) — بس هون بينحكم صح/غلط، وبس من هون بتتغير الحالة.
 * الكشف بيصير بالضغط على خانة الجواب نفسها.
 */
@Composable
fun HostGameBoardScreen(
    state: GameState,
    onCorrect: (Int) -> Unit,
    onWrong: () -> Unit,
    onNextRound: () -> Unit
) {
    val canJudge = state.canJudge()
    val accent = accentFor(state.activeTeam)

    Box {
        StageBackground(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                ScoreHeader(state)
                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.weight(1f)) {
                    // اللوح — عمودين زي شاشة البرنامج.
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        QuestionCard(
                            round = state.currentQuestionIndex + 1,
                            totalRounds = state.questions.size,
                            category = state.currentQuestion?.category,
                            question = state.currentQuestion?.text ?: "—"
                        )
                        Spacer(Modifier.height(10.dp))
                        AnswerBoardColumns(
                            answers = state.currentQuestion?.answers.orEmpty(),
                            revealHiddenText = true,
                            enabledSlots = canJudge,
                            onSlotClick = onCorrect
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // العمود الجانبي: الحالة، الأخطاء، الدور، والأزرار.
                    Column(
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                    ) {
                        StatusBanner(text = hostStatusText(state), accent = accent)
                        Spacer(Modifier.height(10.dp))

                        AwardBanner(state)

                        TurnRail(
                            state = state,
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        )

                        Spacer(Modifier.height(8.dp))
                        SecondaryButton(
                            text = "غلط ✕",
                            onClick = onWrong,
                            enabled = canJudge,
                            accent = FeudColors.strike,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        PrimaryButton(
                            text = state.nextButtonLabel(),
                            onClick = onNextRound,
                            enabled = state.phase == RoundPhase.ROUND_END,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        StrikeFlash(strikes = state.strikes)
    }
}

/** قائمة لاعبين الفريق اللي عليه الدور، مع لون شاشة كل واحد. */
@Composable
private fun TurnRail(state: GameState, modifier: Modifier = Modifier) {
    val team = state.activeTeam ?: return
    val players = state.playersOf(team)
    if (players.isEmpty()) return

    Column(modifier = modifier.padding(top = 8.dp)) {
        Text(
            "دور ${state.teams[team]?.name ?: ""}",
            color = FeudColors.goldDim,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(6.dp))
        players.forEach { player ->
            PlayerRow(player = player, mark = state.markFor(player.id))
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun PlayerRow(player: Player, mark: PlayerMark) {
    val color = when (mark) {
        PlayerMark.ARMED -> FeudColors.gold
        PlayerMark.BUZZED -> Color(0xFF4C9BFF)
        PlayerMark.CORRECT -> Color(0xFF17B65A)
        PlayerMark.WRONG -> FeudColors.strike
        PlayerMark.IDLE -> FeudColors.textMuted
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
            .border(1.5.dp, color.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SeatBadge(seat = player.seat, dim = !player.connected, size = 26.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            player.name,
            color = FeudColors.text,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            markLabel(mark),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.End
        )
    }
}

private fun markLabel(mark: PlayerMark): String = when (mark) {
    PlayerMark.ARMED -> "دوره"
    PlayerMark.BUZZED -> "ضغط"
    PlayerMark.CORRECT -> "صح"
    PlayerMark.WRONG -> "غلط"
    PlayerMark.IDLE -> ""
}

/** المضيف بيقدر يحكم بس لما يكون في لاعب مستنّي حكم. */
private fun GameState.canJudge(): Boolean = when (phase) {
    RoundPhase.FACE_OFF -> buzzedTeam() != null
    RoundPhase.FACE_OFF_SECOND, RoundPhase.PLAY, RoundPhase.STEAL -> true
    else -> false
}

private fun GameState.nextButtonLabel(): String =
    if (isLastRound) "إنهاء اللعبة" else "الجولة الجاية"

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun HostGameBoardScreenPreview() {
    FeudPartyTheme {
        HostGameBoardScreen(
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
                    Player("a1", "سامر", TeamId.TEAM_1, seat = 1),
                    Player("a2", "هناء", TeamId.TEAM_1, seat = 2),
                    Player("a3", "زيد", TeamId.TEAM_1, seat = 3),
                    Player("b1", "ليلى", TeamId.TEAM_2, seat = 1)
                ),
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "الفريق الأحمر", 140, connected = true),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الفريق الأزرق", 95, connected = true)
                ),
                phase = RoundPhase.PLAY,
                controllingTeam = TeamId.TEAM_1,
                turnPlayerId = "a2",
                wrongPlayers = setOf("a3"),
                correctPlayers = setOf("a1"),
                buzzState = BuzzState.CLOSED,
                pot = 40,
                strikes = 2
            ),
            onCorrect = {},
            onWrong = {},
            onNextRound = {}
        )
    }
}
