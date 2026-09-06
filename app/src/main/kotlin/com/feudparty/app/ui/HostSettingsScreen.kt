package com.feudparty.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import com.feudparty.app.settings.GameSettings
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.GoldDivider
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

/**
 * إعدادات المضيف: بنك الأسئلة، عدد الجولات ومضاعفاتها، عدد الأخطاء،
 * وإعدادات الجولة السريعة. كلها بتتطبّق على اللعبة الجاية.
 */
@Composable
fun HostSettingsScreen(
    settings: GameSettings,
    bankMessage: String?,
    bankMessageIsError: Boolean,
    onSettingsChange: (GameSettings) -> Unit,
    onImportBank: (android.net.Uri, String) -> Unit,
    onClearBank: () -> Unit,
    onBack: () -> Unit
) {
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "بنك مستورد"
            onImportBank(uri, name)
        }
    }

    StageBackground(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "الإعدادات",
                    color = FeudColors.gold,
                    style = MaterialTheme.typography.headlineSmall
                )
                SecondaryButton(
                    text = "رجوع",
                    onClick = onBack,
                    accent = FeudColors.teal,
                    modifier = Modifier.width(160.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            GoldDivider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    BankSection(
                        settings = settings,
                        message = bankMessage,
                        isError = bankMessageIsError,
                        onPick = { picker.launch(arrayOf("application/json", "text/plain", "*/*")) },
                        onClear = onClearBank
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    RoundsSection(settings, onSettingsChange)
                    Spacer(Modifier.height(12.dp))
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
}

@Composable
private fun BankSection(
    settings: GameSettings,
    message: String?,
    isError: Boolean,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    SettingsCard(title = "بنك الأسئلة") {
        Text(
            settings.bankName?.let { "الحالي: $it — ${settings.bankQuestionCount.ar()} سؤال" }
                ?: "الحالي: البنك المرفق مع التطبيق",
            color = FeudColors.text,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(10.dp))
        Row {
            PrimaryButton(
                text = "استيراد ملف",
                onClick = onPick,
                modifier = Modifier.weight(1f)
            )
            if (settings.bankName != null) {
                Spacer(Modifier.width(10.dp))
                SecondaryButton(
                    text = "حذف",
                    onClick = onClear,
                    accent = FeudColors.pink,
                    modifier = Modifier.width(120.dp)
                )
            }
        }

        if (message != null) {
            Spacer(Modifier.height(10.dp))
            CartoonSurface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isError) FeudColors.pink else FeudColors.lime,
                borderWidth = 3.dp,
                corner = 12.dp,
                shadow = 4.dp
            ) {
                Text(
                    message,
                    color = if (isError) Color.White else FeudColors.ink,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "بتقدر تستورد أسئلتك من ملف بدل الأسئلة الجاهزة. كل سؤال بدّه " +
                "نص وأجوبة، وكل جواب إله نقاط — والأعلى نقاط بياخد اللوح " +
                "بالمواجهة.",
            color = FeudColors.textMuted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun RoundsSection(settings: GameSettings, onChange: (GameSettings) -> Unit) {
    SettingsCard(title = "الجولات والنقاط") {
        Stepper(
            label = "عدد الجولات",
            value = settings.rounds,
            min = GameSettings.MIN_ROUNDS,
            max = GameSettings.MAX_ROUNDS,
            onChange = { onChange(settings.copy(rounds = it)) }
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "مضاعف كل جولة",
            color = FeudColors.gold,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(6.dp))
        val perRound = settings.multipliersForRounds()
        Column {
            perRound.chunked(4).forEachIndexed { rowIndex, chunk ->
                Row(modifier = Modifier.padding(bottom = 8.dp)) {
                    chunk.forEachIndexed { columnIndex, multiplier ->
                        val index = rowIndex * 4 + columnIndex
                        if (columnIndex > 0) Spacer(Modifier.width(8.dp))
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
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Stepper(
            label = "عدد الأخطاء اللي بتفتح السرقة",
            value = settings.strikesToSteal,
            min = GameSettings.MIN_STRIKES,
            max = GameSettings.MAX_STRIKES,
            onChange = { onChange(settings.copy(strikesToSteal = it)) }
        )
        Spacer(Modifier.height(10.dp))
        Pill(
            text = "بتحتاج ${settings.questionsNeeded().ar()} سؤال باللعبة الوحدة",
            color = FeudColors.gold
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    CartoonSurface(
        modifier = Modifier.fillMaxWidth(),
        color = FeudColors.stageAlt,
        corner = 18.dp,
        shadow = 6.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = FeudColors.gold, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun Stepper(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    step: Int = 1,
    onChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            color = FeudColors.text,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(10.dp))
        StepperButton(text = "−", enabled = value > min) {
            onChange((value - step).coerceAtLeast(min))
        }
        Box(
            modifier = Modifier.width(62.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value.ar(),
                color = FeudColors.gold,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
        StepperButton(text = "+", enabled = value < max) {
            onChange((value + step).coerceAtMost(max))
        }
    }
}

@Composable
private fun StepperButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    CartoonSurface(
        color = if (enabled) FeudColors.gold else FeudColors.panelDark,
        borderWidth = 3.dp,
        corner = 12.dp,
        shadow = 4.dp,
        onClick = onClick,
        enabled = enabled
    ) {
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            Text(
                text,
                color = if (enabled) FeudColors.ink else FeudColors.outlineSoft,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun MultiplierChip(
    round: Int,
    multiplier: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CartoonSurface(
        modifier = modifier,
        color = when (multiplier) {
            1 -> FeudColors.stage
            2 -> FeudColors.teal
            3 -> FeudColors.lime
            else -> FeudColors.pink
        },
        borderWidth = 3.dp,
        corner = 12.dp,
        shadow = 4.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "جولة ${round.ar()}",
                color = if (multiplier == 1) FeudColors.textMuted else FeudColors.ink,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                "×${multiplier.ar()}",
                color = if (multiplier == 1) FeudColors.text else FeudColors.ink,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun HostSettingsScreenPreview() {
    FeudPartyTheme {
        HostSettingsScreen(
            settings = GameSettings(bankName = "asilati.json", bankQuestionCount = 62),
            bankMessage = "انقرأ البنك: ٦٢ سؤال",
            bankMessageIsError = false,
            onSettingsChange = {},
            onImportBank = { _, _ -> },
            onClearBank = {},
            onBack = {}
        )
    }
}
