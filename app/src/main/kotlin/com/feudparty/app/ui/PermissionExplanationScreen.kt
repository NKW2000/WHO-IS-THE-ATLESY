package com.feudparty.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

@Composable
fun PermissionExplanationScreen(
    permanentlyDenied: Boolean = false,
    onRequestClick: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    StageBackground(contentPadding = PaddingValues(24.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        Text(
            "بدنا صلاحية قبل ما نبلّش",
            color = FeudColors.gold,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "التطبيق بيربط أجهزتكم مع بعض مباشرة عبر البلوتوث والواي فاي — " +
                "بدون إنترنت وبدون سيرفر خارجي، وما منجمع ولا منبعت أي بيانات لبرا.",
            color = FeudColors.textMuted,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        if (permanentlyDenied) {
            Text(
                "الصلاحية مرفوضة نهائياً — لازم تفعّلها من إعدادات التطبيق.",
                color = FeudColors.strike,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = "فتح إعدادات التطبيق",
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            PrimaryButton(
                text = "منح الصلاحيات",
                onClick = onRequestClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionExplanationScreenPreview() {
    FeudPartyTheme { PermissionExplanationScreen(onRequestClick = {}) }
}
