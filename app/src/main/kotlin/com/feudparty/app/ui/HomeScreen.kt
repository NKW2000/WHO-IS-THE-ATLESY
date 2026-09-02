package com.feudparty.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.GoldDivider
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

@Composable
fun HomeScreen(
    onHostClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    StageBackground(contentPadding = PaddingValues(24.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(FeudColors.gold.copy(alpha = 0.10f), RoundedCornerShape(22.dp))
                    .border(3.dp, FeudColors.gold, RoundedCornerShape(22.dp))
                    .padding(horizontal = 28.dp, vertical = 22.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "تحدي العائلة",
                        color = FeudColors.gold,
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    GoldDivider(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "٤ جولات + الجولة السريعة",
                        color = FeudColors.textMuted,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "جهاز واحد للمضيف، وجهاز لكل فريق — بدون إنترنت.",
                color = FeudColors.textMuted,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))
            PrimaryButton(
                text = "استضافة لعبة",
                onClick = onHostClick,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            SecondaryButton(
                text = "انضمام كفريق",
                onClick = onJoinClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun HomeScreenPreview() {
    FeudPartyTheme { HomeScreen(onHostClick = {}, onJoinClick = {}) }
}
