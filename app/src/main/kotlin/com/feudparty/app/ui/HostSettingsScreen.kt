package com.feudparty.app.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.feudparty.app.ui.theme.FeudShape
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
    onContinue: () -> Unit,
    answerBounds: IntRange = GameSettings.MIN_ANSWERS..GameSettings.MAX_ANSWERS,
    matchingQuestions: Int = 0
) {
    var renaming by remember { mutableStateOf<TeamId?>(null) }
    var renamingRoom by remember { mutableStateOf(false) }

    // البنك بيحكم الفلتر: إذا تغيّر، منرجّع الأرقام والتصنيفات لحدوده.
    LaunchedEffect(answerBounds) {
        val low = settings.minAnswers.coerceIn(answerBounds)
        val high = settings.maxAnswers.coerceIn(low, answerBounds.last)
        if (low != settings.minAnswers || high != settings.maxAnswers) {
            onSettingsChange(settings.copy(minAnswers = low, maxAnswers = high))
        }
    }

    // البنك بيحكم الفلتر: إذا تغيّر، منرجّع الأرقام لحدوده.
    LaunchedEffect(answerBounds) {
        val low = settings.minAnswers.coerceIn(answerBounds)
        val high = settings.maxAnswers.coerceIn(low, answerBounds.last)
        if (low != settings.minAnswers || high != settings.maxAnswers) {
            onSettingsChange(settings.copy(minAnswers = low, maxAnswers = high))
        }
    }

    // نفس التصميم بالوضعين: شريط علوي، جسم بيتمرّر، وشريط سفلي.
    DesignHostSettings(
        settings = settings,
        answerBounds = answerBounds,
        matchingQuestions = matchingQuestions,
        onSettingsChange = onSettingsChange,
        onBack = onBack,
        onContinue = onContinue,
        onRenameRoom = { renamingRoom = true },
        onRenameTeam = { renaming = it }
    )

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

/**
 * إعدادات المضيف بالوضع الطولي — نفس كرت التصميم: شريط علوي فيه زر
 * الرجوع والعنوان، جسم بيتمرّر بأقسام (الأسماء، الجولات، الوقت والأخطاء،
 * تصفية الأسئلة)، وشريط سفلي فيه «كمّل للوبي».
 */
@Composable
private fun DesignHostSettings(
    settings: GameSettings,
    answerBounds: IntRange,
    matchingQuestions: Int,
    onSettingsChange: (GameSettings) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onRenameRoom: () -> Unit,
    onRenameTeam: (TeamId) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FeudColors.stage)
    ) {
        // شريط علوي.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = FeudColors.ink,
                        topLeft = Offset(0f, size.height - 4.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(size.width, 4.dp.toPx())
                    )
                }
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CartoonSurface(
                color = FeudColors.teal,
                borderWidth = 3.dp,
                corner = FeudShape.block,
                shadow = 4.dp,
                onClick = onBack
            ) {
                Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                    Text("‹", color = FeudColors.ink, style = MaterialTheme.typography.titleLarge)
                }
            }
            Text(
                "إعدادات اللعبة",
                color = FeudColors.gold,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        // بالعرضي منحدّد عرض العمود حتى ما تصير الخانات شريط طويل فاضي.
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterHorizontally)
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // ---- الأسماء
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("الأسماء")
                NameLine("الغرفة", settings.roomName, FeudColors.gold, FeudColors.ink, onRenameRoom)
                TeamId.entries.forEachIndexed { index, teamId ->
                    NameLine(
                        label = "فريق ${(index + 1).ar()}",
                        name = settings.teamName(teamId),
                        color = teamId.color(),
                        ink = teamId.inkColor(),
                        onRename = { onRenameTeam(teamId) }
                    )
                }
            }

            // ---- الجولات
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel("الجولات")
                SettingRow(
                    label = "عدد الجولات",
                    value = settings.rounds.ar(),
                    onMinus = {
                        onSettingsChange(
                            settings.copy(rounds = (settings.rounds - 1)
                                .coerceAtLeast(GameSettings.MIN_ROUNDS))
                        )
                    },
                    onPlus = {
                        onSettingsChange(
                            settings.copy(rounds = (settings.rounds + 1)
                                .coerceAtMost(GameSettings.MAX_ROUNDS))
                        )
                    }
                )
                val perRound = settings.multipliersForRounds()
                perRound.chunked(4).forEachIndexed { rowIndex, chunk ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        chunk.forEachIndexed { columnIndex, multiplier ->
                            val index = rowIndex * 4 + columnIndex
                            MultiplierTile(
                                round = index + 1,
                                multiplier = multiplier,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val next = perRound.toMutableList()
                                    next[index] = if (multiplier >= 4) 1 else multiplier + 1
                                    onSettingsChange(settings.copy(multipliers = next))
                                }
                            )
                        }
                        repeat(4 - chunk.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                Text(
                    "دوس على الجولة تبدّل مضاعفها",
                    color = FeudColors.textFaint,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // ---- الوقت والأخطاء
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel("الوقت والأخطاء")
                SettingRow(
                    label = "ثواني الجواب",
                    value = settings.answerSeconds.ar(),
                    onMinus = {
                        onSettingsChange(
                            settings.copy(answerSeconds = (settings.answerSeconds - 5)
                                .coerceAtLeast(GameSettings.MIN_ANSWER_SECONDS))
                        )
                    },
                    onPlus = {
                        onSettingsChange(
                            settings.copy(answerSeconds = (settings.answerSeconds + 5)
                                .coerceAtMost(GameSettings.MAX_ANSWER_SECONDS))
                        )
                    }
                )
                SettingRow(
                    label = "ثواني «العب أو تمرير»",
                    value = settings.choiceSeconds.ar(),
                    onMinus = {
                        onSettingsChange(
                            settings.copy(choiceSeconds = (settings.choiceSeconds - 1)
                                .coerceAtLeast(GameSettings.MIN_CHOICE_SECONDS))
                        )
                    },
                    onPlus = {
                        onSettingsChange(
                            settings.copy(choiceSeconds = (settings.choiceSeconds + 1)
                                .coerceAtMost(GameSettings.MAX_CHOICE_SECONDS))
                        )
                    }
                )
                SettingRow(
                    label = "أخطاء تفتح السرقة",
                    value = settings.strikesToSteal.ar(),
                    onMinus = {
                        onSettingsChange(
                            settings.copy(strikesToSteal = (settings.strikesToSteal - 1)
                                .coerceAtLeast(GameSettings.MIN_STRIKES))
                        )
                    },
                    onPlus = {
                        onSettingsChange(
                            settings.copy(strikesToSteal = (settings.strikesToSteal + 1)
                                .coerceAtMost(GameSettings.MAX_STRIKES))
                        )
                    }
                )
            }

            // ---- تصفية الأسئلة
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionLabel("تصفية الأسئلة", modifier = Modifier.weight(1f))
                    Pill(
                        text = "${matchingQuestions.ar()} سؤال مطابق",
                        color = if (matchingQuestions >= settings.rounds) {
                            FeudColors.lime
                        } else {
                            FeudColors.pink
                        },
                        textColor = if (matchingQuestions >= settings.rounds) {
                            FeudColors.ink
                        } else {
                            FeudColors.cream
                        }
                    )
                }
                SettingRow(
                    label = "عدد الأجوبة",
                    value = "${settings.minAnswers.ar()} ‑ ${settings.maxAnswers.ar()}",
                    valueWidth = 68.dp,
                    onMinus = {
                        onSettingsChange(
                            settings.copy(
                                minAnswers = (settings.minAnswers - 1)
                                    .coerceAtLeast(answerBounds.first)
                            )
                        )
                    },
                    onPlus = {
                        onSettingsChange(
                            settings.copy(
                                maxAnswers = (settings.maxAnswers + 1)
                                    .coerceAtMost(answerBounds.last),
                                minAnswers = if (settings.maxAnswers >= answerBounds.last) {
                                    (settings.minAnswers + 1)
                                        .coerceAtMost(settings.maxAnswers)
                                } else {
                                    settings.minAnswers
                                }
                            )
                        )
                    }
                )
            }
        }

        // شريط سفلي.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FeudColors.panelDark)
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 18.dp)
        ) {
            PrimaryButton(
                text = "كمّل للوبي",
                onClick = onContinue,
                color = FeudColors.lime,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
            )
        }
    }
}

/** عنوان قسم صغير بالذهبي. */
@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        color = FeudColors.gold,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
    )
}

/** سطر اسم: تسمية صغيرة، الاسم بلون، وزر قلم. */
@Composable
private fun NameLine(
    label: String,
    name: String,
    color: Color,
    ink: Color,
    onRename: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            label,
            color = FeudColors.textMuted,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(44.dp)
        )
        CartoonSurface(
            modifier = Modifier.weight(1f),
            color = color,
            borderWidth = 3.dp,
            corner = FeudShape.block,
            shadow = 4.dp,
            onClick = onRename
        ) {
            Text(
                name,
                color = ink,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
        CartoonSurface(
            color = FeudColors.stageAlt,
            borderWidth = 3.dp,
            corner = FeudShape.block,
            shadow = 0.dp,
            onClick = onRename
        ) {
            Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                Text("✎", color = FeudColors.cream, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

/** سطر إعداد: التسمية عاليمين وعدّاد − قيمة + عالشمال. */
@Composable
private fun SettingRow(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    valueWidth: Dp = 52.dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            label,
            color = FeudColors.cream,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        CartoonSurface(
            color = FeudColors.stageAlt,
            borderWidth = 3.dp,
            corner = FeudShape.block,
            shadow = 4.dp
        ) {
            // الزراير جوّا الإطار: منقصّها على نفس الاستدارة حتى ما تطلع
            // من زواياه.
            Row(
                modifier = Modifier
                    .padding(3.dp)
                    .clip(RoundedCornerShape(FeudShape.block)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepKey("−", onMinus)
                Text(
                    value,
                    color = FeudColors.gold,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.width(valueWidth)
                )
                StepKey("+", onPlus)
            }
        }
    }
}

/** مربّع ذهبي بعلامة زائد أو ناقص. */
@Composable
private fun StepKey(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(FeudColors.gold)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = FeudColors.ink, style = MaterialTheme.typography.titleLarge)
    }
}

/** مربّع مضاعف الجولة. */
@Composable
private fun MultiplierTile(
    round: Int,
    multiplier: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = when (multiplier) {
        1 -> FeudColors.stage
        2 -> FeudColors.teal
        3 -> FeudColors.lime
        else -> FeudColors.gold
    }
    val ink = if (multiplier == 1) FeudColors.cream else FeudColors.ink
    CartoonSurface(
        modifier = modifier,
        color = color,
        borderWidth = 3.dp,
        corner = FeudShape.block,
        shadow = 4.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("×${multiplier.ar()}", color = ink, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "جولة ${round.ar()}",
                color = ink.copy(alpha = 0.65f),
                style = MaterialTheme.typography.labelSmall
            )
        }
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
            corner = FeudShape.block,
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
            corner = FeudShape.block,
            shadow = 4.dp,
            onClick = onRename
        ) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Text("✎", color = FeudColors.ink, style = MaterialTheme.typography.titleMedium)
            }
        }
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
