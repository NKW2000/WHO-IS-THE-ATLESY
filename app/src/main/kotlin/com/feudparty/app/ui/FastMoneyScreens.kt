package com.feudparty.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.GoldDivider
import com.feudparty.app.ui.components.GoldPanel
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.Answer
import com.feudparty.core.game.FastMoneyEntry
import com.feudparty.core.game.FastMoneyState
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState

/**
 * الجولة السريعة عند المضيف — هو اللي بيسمع جواب اللاعب وبيدوس على الخانة
 * المطابقة (أو «ما جاوب»)، والوقت بيمشي عنده.
 */
@Composable
fun FastMoneyHostScreen(
    state: GameState,
    onStartTimer: () -> Unit,
    onSubmit: (Int?) -> Unit,
    onReveal: () -> Unit,
    onEndGame: () -> Unit
) {
    val fastMoney = state.fastMoney ?: return
    val teamName = state.teams[fastMoney.teamId]?.name ?: ""
    val accent = fastMoney.teamId.color()

    StageBackground(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FastMoneyHeader(fastMoney, teamName, accent, revealTotal = true)
            Spacer(Modifier.height(12.dp))

            if (fastMoney.finished) {
                FastMoneySummary(fastMoney)
                Spacer(Modifier.height(16.dp))
                if (!fastMoney.revealed) {
                    PrimaryButton(
                        text = "اكشف النتيجة للفرق",
                        onClick = onReveal,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    PrimaryButton(
                        text = "إنهاء اللعبة",
                        onClick = onEndGame,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                return@Column
            }

            TimerRing(
                seconds = fastMoney.secondsRemaining,
                total = if (fastMoney.playerIndex == 0) {
                    FastMoneyState.FIRST_PLAYER_SECONDS
                } else {
                    FastMoneyState.SECOND_PLAYER_SECONDS
                },
                running = fastMoney.timerRunning,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(12.dp))

            if (!fastMoney.timerRunning) {
                PrimaryButton(
                    text = if (fastMoney.currentEntries.isEmpty()) "ابدأ الوقت" else "كمّل الوقت",
                    onClick = onStartTimer,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }

            GoldPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "سؤال ${fastMoney.questionIndex + 1} من ${fastMoney.questions.size}",
                        color = FeudColors.goldDim,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        fastMoney.currentQuestion?.text ?: "—",
                        color = FeudColors.text,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            if (fastMoney.duplicateFlag) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FeudColors.strike.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                        .border(2.dp, FeudColors.strike, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "الجواب مكرر — خلّي اللاعب يعطي جواب تاني",
                        color = FeudColors.strike,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            fastMoney.currentQuestion?.answers?.forEachIndexed { index, answer ->
                val used = fastMoney.playerIndex == 1 && index in fastMoney.usedByPlayerOne
                FastMoneyAnswerRow(
                    answer = answer,
                    used = used,
                    onClick = { onSubmit(index) }
                )
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(6.dp))
            SecondaryButton(
                text = "ما جاوب / جواب مش موجود",
                onClick = { onSubmit(null) },
                accent = FeudColors.strike,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** شاشة الفريق أثناء الجولة السريعة — بتتفرّج بس. */
@Composable
fun FastMoneyTeamScreen(state: GameState, myTeam: TeamId?) {
    val fastMoney = state.fastMoney ?: return
    val mine = fastMoney.teamId == myTeam
    val teamName = state.teams[fastMoney.teamId]?.name ?: ""
    val accent = fastMoney.teamId.color()

    StageBackground(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FastMoneyHeader(fastMoney, teamName, accent, revealTotal = fastMoney.revealed)
            Spacer(Modifier.height(16.dp))

            Pill(
                text = if (mine) "دوركم — جاوبوا بصوت عالي للمضيف" else "الفريق التاني بيلعب",
                color = accent
            )
            Spacer(Modifier.height(20.dp))

            if (!fastMoney.finished) {
                TimerRing(
                    seconds = fastMoney.secondsRemaining,
                    total = if (fastMoney.playerIndex == 0) {
                        FastMoneyState.FIRST_PLAYER_SECONDS
                    } else {
                        FastMoneyState.SECOND_PLAYER_SECONDS
                    },
                    running = fastMoney.timerRunning
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    "اللاعب ${fastMoney.playerIndex + 1} — سؤال ${fastMoney.questionIndex + 1}/${fastMoney.questions.size}",
                    color = FeudColors.textMuted,
                    style = MaterialTheme.typography.titleMedium
                )
            } else if (fastMoney.revealed) {
                FastMoneySummary(fastMoney)
            } else {
                Text(
                    "خلصت الأجوبة — بانتظار المضيف يكشف النتيجة",
                    color = FeudColors.textMuted,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FastMoneyHeader(
    fastMoney: FastMoneyState,
    teamName: String,
    accent: Color,
    revealTotal: Boolean
) {
    val total by animateIntAsState(
        targetValue = if (revealTotal) fastMoney.total else 0,
        animationSpec = tween(500),
        label = "fmTotal"
    )
    val progress by animateFloatAsState(
        targetValue = if (revealTotal) {
            (fastMoney.total.toFloat() / FastMoneyState.TARGET).coerceIn(0f, 1f)
        } else {
            0f
        },
        animationSpec = tween(500),
        label = "fmProgress"
    )

    GoldPanel(modifier = Modifier.fillMaxWidth(), accent = accent) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الجولة السريعة", color = FeudColors.gold, style = MaterialTheme.typography.titleLarge)
                Text(teamName, color = accent, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
            GoldDivider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    if (revealTotal) "$total" else "؟؟",
                    color = FeudColors.text,
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    " / ${FastMoneyState.TARGET}",
                    color = FeudColors.goldDim,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.weight(1f))
                if (revealTotal && fastMoney.finished) {
                    Pill(
                        text = if (fastMoney.won) "فوز!" else "ما وصلوا",
                        color = if (fastMoney.won) FeudColors.gold else FeudColors.strike
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(FeudColors.panel, RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(FeudColors.gold, RoundedCornerShape(5.dp))
                )
            }
        }
    }
}

@Composable
private fun TimerRing(
    seconds: Int,
    total: Int,
    running: Boolean,
    modifier: Modifier = Modifier
) {
    val danger = seconds <= 5
    val color = when {
        danger -> FeudColors.strike
        running -> FeudColors.gold
        else -> FeudColors.goldDim
    }
    val fraction by animateFloatAsState(
        targetValue = (seconds.toFloat() / total).coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "timer"
    )

    Box(
        modifier = modifier.size(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color.copy(alpha = 0.10f + 0.25f * fraction), CircleShape)
                .border(6.dp, color, CircleShape)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$seconds", color = color, style = MaterialTheme.typography.displayMedium)
            Text("ثانية", color = FeudColors.textMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun FastMoneyAnswerRow(answer: Answer, used: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (used) FeudColors.panel.copy(alpha = 0.5f) else FeudColors.panel,
                RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (used) FeudColors.strike.copy(alpha = 0.5f) else FeudColors.goldDim,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            answer.text,
            color = if (used) FeudColors.textMuted else FeudColors.text,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (used) {
            Text("مستعمل", color = FeudColors.strike, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(8.dp))
        }
        Text("${answer.points}", color = FeudColors.gold, style = MaterialTheme.typography.titleLarge)
    }
}

/** جدول أجوبة اللاعبين بعد ما يخلصوا. */
@Composable
private fun FastMoneySummary(fastMoney: FastMoneyState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        PlayerColumn("اللاعب ١", fastMoney.playerOne, fastMoney.playerOneTotal, fastMoney.questions)
        Spacer(Modifier.height(10.dp))
        PlayerColumn("اللاعب ٢", fastMoney.playerTwo, fastMoney.playerTwoTotal, fastMoney.questions)
    }
}

@Composable
private fun PlayerColumn(
    title: String,
    entries: List<FastMoneyEntry>,
    total: Int,
    questions: List<Question>
) {
    GoldPanel(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, color = FeudColors.gold, style = MaterialTheme.typography.titleMedium)
                Text("$total", color = FeudColors.text, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(6.dp))
            entries.forEachIndexed { index, entry ->
                val answerText = entry.answerIndex
                    ?.let { questions.getOrNull(index)?.answers?.getOrNull(it)?.text }
                    ?: "—"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${index + 1}. $answerText",
                        color = if (entry.passed) FeudColors.textMuted else FeudColors.text,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${entry.points}",
                        color = if (entry.points > 0) FeudColors.gold else FeudColors.textMuted,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun FastMoneyHostPreview() {
    val question = Question(
        "f1",
        "اذكر شي بيوخّر الناس عن الدوام",
        listOf(Answer("الزحمة", 40), Answer("النوم", 30), Answer("المطر", 15)),
        "عام"
    )
    FeudPartyTheme {
        FastMoneyHostScreen(
            state = GameState(
                questions = emptyList(),
                teams = mapOf(
                    TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "النجوم", score = 320, connected = true),
                    TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الصقور", score = 210, connected = true)
                ),
                phase = RoundPhase.FAST_MONEY,
                fastMoney = FastMoneyState(
                    questions = List(5) { question },
                    teamId = TeamId.TEAM_1,
                    playerOne = listOf(FastMoneyEntry(0, 40)),
                    questionIndex = 1,
                    secondsRemaining = 14,
                    timerRunning = true
                )
            ),
            onStartTimer = {},
            onSubmit = {},
            onReveal = {},
            onEndGame = {}
        )
    }
}
