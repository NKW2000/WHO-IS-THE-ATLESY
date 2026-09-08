package com.feudparty.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.settings.GameSettings
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.GoldDivider
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.SettingsCard
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

/**
 * الإعدادات العامة — استيراد بنك الأسئلة وبس. باقي إعدادات اللعبة (الفرق،
 * الجولات، وقت الجواب، الأخطاء) بتتظبّط عند المضيف لما يستضيف لعبة.
 */
@Composable
fun BankSettingsScreen(
    settings: GameSettings,
    bankMessage: String?,
    bankMessageIsError: Boolean,
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

    StageBackground(contentPadding = stagePadding()) {
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
                    modifier = Modifier.width(150.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            GoldDivider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                SettingsCard(title = "بنك الأسئلة") {
                    Text(
                        settings.bankName?.let {
                            "الحالي: $it — ${settings.bankQuestionCount.ar()} سؤال"
                        } ?: "الحالي: البنك المرفق مع التطبيق",
                        color = FeudColors.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(10.dp))
                    Row {
                        PrimaryButton(
                            text = "استيراد ملف",
                            onClick = {
                                picker.launch(arrayOf("application/json", "text/plain", "*/*"))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (settings.bankName != null) {
                            Spacer(Modifier.width(10.dp))
                            SecondaryButton(
                                text = "حذف",
                                onClick = onClearBank,
                                accent = FeudColors.pink,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }

                    if (bankMessage != null) {
                        Spacer(Modifier.height(10.dp))
                        CartoonSurface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (bankMessageIsError) FeudColors.pink else FeudColors.lime,
                            borderWidth = 3.dp,
                            corner = 12.dp,
                            shadow = 4.dp
                        ) {
                            Text(
                                bankMessage,
                                color = if (bankMessageIsError) Color.White else FeudColors.ink,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "بتقدر تستورد أسئلتك من ملف بدل الأسئلة الجاهزة. كل سؤال بدّه نص " +
                            "وأجوبة، وكل جواب إله نقاط — والأعلى نقاط بياخد اللوح بالمواجهة.",
                        color = FeudColors.textMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun BankSettingsScreenPreview() {
    FeudPartyTheme {
        BankSettingsScreen(
            settings = GameSettings(bankName = "asilati.json", bankQuestionCount = 62),
            bankMessage = "انقرأ البنك: ٦٢ سؤال",
            bankMessageIsError = false,
            onImportBank = { _, _ -> },
            onClearBank = {},
            onBack = {}
        )
    }
}
