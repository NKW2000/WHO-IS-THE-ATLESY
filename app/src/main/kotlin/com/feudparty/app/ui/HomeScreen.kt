package com.feudparty.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.Wordmark
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme

@Composable
fun HomeScreen(
    onHostClick: () -> Unit,
    onJoinClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    StageBackground(contentPadding = PaddingValues(horizontal = 30.dp, vertical = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Wordmark()
                Spacer(Modifier.height(18.dp))
                Pill(
                    text = "جهاز للمضيف · جهاز لكل لاعب · بدون إنترنت",
                    color = FeudColors.ink.copy(alpha = 0.55f),
                    textColor = FeudColors.text
                )
            }

            Spacer(Modifier.width(26.dp))

            Column(
                modifier = Modifier.width(300.dp),
                verticalArrangement = Arrangement.Center
            ) {
                PrimaryButton(
                    text = "استضافة لعبة",
                    onClick = onHostClick,
                    color = FeudColors.lime,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                SecondaryButton(
                    text = "انضمام كلاعب",
                    onClick = onJoinClick,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                SecondaryButton(
                    text = "الإعدادات",
                    onClick = onSettingsClick,
                    accent = FeudColors.gold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun HomeScreenPreview() {
    FeudPartyTheme { HomeScreen(onHostClick = {}, onJoinClick = {}) }
}
