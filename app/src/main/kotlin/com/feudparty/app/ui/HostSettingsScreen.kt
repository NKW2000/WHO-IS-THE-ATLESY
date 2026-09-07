package com.feudparty.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.settings.GameSettings
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.SettingsCard
import com.feudparty.app.ui.components.Stepper
import com.feudparty.app.ui.components.MultiplierChip
import com.feudparty.app.ui.components.GoldDivider
import com.feudparty.app.ui.components.NamePromptDialog
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.components.inkColor
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.TeamId

/**
 * إعدادات المضيف — أول شاشة بيشوفها لما يضغط «استضافة لعبة»: أسماء
 * الفريقين، بنك الأسئلة، عدد الجولات ومضاعفاتها، وقت الجواب، وعدد
 * الأخطاء اللي بتفتح السرقة. بعدها بيكمّل عاللوبي.
 */
@Composable
fun HostSettingsScreen(
    settings: GameSettings,
    onSettingsChange: (GameSettings) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    var renaming by remember { mutableStateOf<TeamId?>(null) }
    var renamingRoom by remember { mutableStateOf(false) }

    StageBackground(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "إعدادات اللعبة",
                    color = FeudColors.gold,
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SecondaryButton(
                        text = "رجوع",
                        onClick = onBack,
                        accent = FeudColors.teal,
                        modifier = Modifier.width(150.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    PrimaryButton(
                        text = "كمّل للوبي",
                        onClick = onContinue,
                        color = FeudColors.lime,
                        modifier = Modifier.width(190.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            GoldDivider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            // تلات أعمدة متساوية، وكل عمود بطاقاته بتملا نفس الارتفاع —
            // فبتضل الرؤوس والحواف على خط واحد وما في تمرير.
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        RoomSection(settings) { renamingRoom = true }
                    }
                    Box(modifier = Modifier.weight(2f)) {
                        TeamsSection(settings) { renaming = it }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    RoundsSection(
                        settings = settings,
                        onChange = onSettingsChange,
                        modifier = Modifier.fillMaxHeight()
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimingSection(
                        settings = settings,
                        onChange = onSettingsChange,
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryButton(
                        text = "رجوع للإعدادات الافتراضية",
                        onClick = { onSettingsChange(GameSettings()) },
                        accent = FeudColors.pink,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (renamingRoom) {
        NamePromptDialog(
            title = "اسم الغرفة",
            initial = settings.roomName,
            maxLength = GameSettings.MAX_TEAM_NAME,
            onConfirm = { name ->
                onSettingsChange(settings.copy(roomName = name))
                renamingRoom = false
            },
            onDismiss = { renamingRoom = false }
        )
    }

    renaming?.let { teamId ->
        NamePromptDialog(
            title = "اسم الفريق",
            initial = settings.teamName(teamId),
            maxLength = GameSettings.MAX_TEAM_NAME,
            onConfirm = { name ->
                onSettingsChange(
                    settings.copy(teamNames = settings.teamNames + (teamId to name))
                )
                renaming = null
            },
            onDismiss = { renaming = null }
        )
    }
}

/** اسم الغرفة — هو اللي بيبيّن باللستة عند اللاعبين. */
@Composable
private fun RoomSection(settings: GameSettings, onRename: () -> Unit) {
    SettingsCard(title = "الغرفة", modifier = Modifier.fillMaxHeight()) {
        NameField(
            name = settings.roomName,
            color = FeudColors.gold,
            ink = FeudColors.ink,
            onRename = onRename
        )
    }
}

/** اسم بسطر واحد وجنبه زر قلم صغير — بيوفّر ارتفاع وما بينقص الاسم. */
@Composable
private fun NameField(
    name: String,
    color: Color,
    ink: Color,
    onRename: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CartoonSurface(
            modifier = Modifier.weight(1f),
            color = color,
            borderWidth = 3.dp,
            corner = 12.dp,
            shadow = 4.dp
        ) {
            Text(
                name,
                color = ink,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        CartoonSurface(
            color = FeudColors.teal,
            borderWidth = 3.dp,
            corner = 12.dp,
            shadow = 4.dp,
            onClick = onRename
        ) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Text("✎", color = FeudColors.ink, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/** أسماء الفريقين — المضيف بيسمّيهم قبل ما يفوتوا اللاعبين. */
@Composable
private fun TeamsSection(settings: GameSettings, onRename: (TeamId) -> Unit) {
    SettingsCard(title = "الفريقين", modifier = Modifier.fillMaxHeight()) {
        TeamId.entries.forEachIndexed { index, teamId ->
            if (index > 0) Spacer(Modifier.height(8.dp))
            NameField(
                name = settings.teamName(teamId),
                color = teamId.color(),
                ink = teamId.inkColor(),
                onRename = { onRename(teamId) }
            )
        }
    }
}

@Composable
private fun RoundsSection(
    settings: GameSettings,
    onChange: (GameSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(title = "الجولات والنقاط", modifier = modifier) {
        Stepper(
            label = "عدد الجولات",
            value = settings.rounds,
            min = GameSettings.MIN_ROUNDS,
            max = GameSettings.MAX_ROUNDS,
            onChange = { onChange(settings.copy(rounds = it)) }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "مضاعف كل جولة",
            color = FeudColors.gold,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(6.dp))
        val perRound = settings.multipliersForRounds()
        Column {
            perRound.chunked(4).forEachIndexed { rowIndex, chunk ->
                Row(modifier = Modifier.padding(bottom = 6.dp)) {
                    chunk.forEachIndexed { columnIndex, multiplier ->
                        val index = rowIndex * 4 + columnIndex
                        if (columnIndex > 0) Spacer(Modifier.width(6.dp))
                        MultiplierChip(
                            round = index + 1,
                            multiplier = multiplier,
                            onClick = {
                                val next = perRound.toMutableList()
                                // ١ ← ٢ ← ٣ ← ٤ ← رجوع لـ ١
                                next[index] = if (multiplier >= 4) 1 else multiplier + 1
                                onChange(settings.copy(multipliers = next))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - chunk.size) {
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Pill(
            text = "بتحتاج ${settings.questionsNeeded().ar()} سؤال باللعبة الوحدة",
            color = FeudColors.gold
        )
    }
}

/** التوقيت والأخطاء — عمود لحاله حتى يوصله المضيف بدون تمرير. */
@Composable
private fun TimingSection(
    settings: GameSettings,
    onChange: (GameSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(title = "الوقت والأخطاء", modifier = modifier) {
        Stepper(
            label = "ثواني الجواب",
            value = settings.answerSeconds,
            min = GameSettings.MIN_ANSWER_SECONDS,
            max = GameSettings.MAX_ANSWER_SECONDS,
            step = 5,
            onChange = { onChange(settings.copy(answerSeconds = it)) }
        )
        Spacer(Modifier.height(6.dp))
        Stepper(
            label = "ثواني «العب أو تمرير»",
            value = settings.choiceSeconds,
            min = GameSettings.MIN_CHOICE_SECONDS,
            max = GameSettings.MAX_CHOICE_SECONDS,
            onChange = { onChange(settings.copy(choiceSeconds = it)) }
        )
        Spacer(Modifier.height(6.dp))
        Stepper(
            label = "أخطاء تفتح السرقة",
            value = settings.strikesToSteal,
            min = GameSettings.MIN_STRIKES,
            max = GameSettings.MAX_STRIKES,
            onChange = { onChange(settings.copy(strikesToSteal = it)) }
        )
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun HostSettingsScreenPreview() {
    FeudPartyTheme {
        HostSettingsScreen(
            settings = GameSettings(bankName = "asilati.json", bankQuestionCount = 62),
            onSettingsChange = {},
            onBack = {},
            onContinue = {}
        )
    }
}
