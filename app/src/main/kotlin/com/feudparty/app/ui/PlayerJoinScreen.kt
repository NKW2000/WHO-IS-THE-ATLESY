package com.feudparty.app.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.NameKeypad
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.components.color
import com.feudparty.app.ui.components.inkColor
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.core.game.TeamId

/**
 * انضمام لاعب: الاسم بينكتب بكيبورد التطبيق نفسه — كيبورد النظام بينقسم
 * نصين بالوضع الأفقي وهاد إعداد بالكيبورد مش بإيدنا، ولاسم قصير ما
 * منحتاجه أصلاً. وبعد الاسم بيختار فريقه.
 */
@Composable
fun PlayerJoinScreen(
    teamNames: Map<TeamId, String> = defaultTeamNames,
    onJoinConfirmed: (String, TeamId) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var team by remember { mutableStateOf<TeamId?>(null) }
    val ready = name.isNotBlank() && team != null

    StageBackground(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "شو اسمك؟",
                    color = FeudColors.gold,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.width(14.dp))
                CartoonSurface(
                    modifier = Modifier.weight(1f),
                    color = FeudColors.cream,
                    corner = 14.dp,
                    shadow = 6.dp
                ) {
                    Text(
                        name.ifBlank { "اكتب اسمك" },
                        color = if (name.isBlank()) {
                            FeudColors.ink.copy(alpha = 0.35f)
                        } else {
                            FeudColors.ink
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "اختار فريقك:",
                    color = FeudColors.textMuted,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(end = 12.dp)
                )
                TeamId.entries.forEachIndexed { index, teamId ->
                    if (index > 0) Spacer(Modifier.width(10.dp))
                    TeamChoice(
                        name = teamNames[teamId] ?: "فريق",
                        teamId = teamId,
                        selected = team == teamId,
                        onClick = { team = teamId },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            NameKeypad(
                onKey = { key -> if (name.length < 14) name += key },
                onBackspace = { name = name.dropLast(1) },
                onDone = { team?.let { if (ready) onJoinConfirmed(name.trim(), it) } },
                doneEnabled = ready,
                doneText = when {
                    name.isBlank() -> "اكتب اسمك"
                    team == null -> "اختار فريق"
                    else -> "يلا نلعب"
                }
            )
        }
    }
}

/** زر فريق — بيضوي لما ينتخب. */
@Composable
private fun TeamChoice(
    name: String,
    teamId: TeamId,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CartoonSurface(
        modifier = modifier,
        color = if (selected) teamId.color() else FeudColors.ink.copy(alpha = 0.4f),
        borderWidth = 4.dp,
        corner = 14.dp,
        shadow = 5.dp,
        onClick = onClick
    ) {
        Text(
            name,
            color = if (selected) teamId.inkColor() else FeudColors.textMuted,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        )
    }
}

internal val defaultTeamNames = mapOf(
    TeamId.TEAM_1 to "الفريق الأخضر",
    TeamId.TEAM_2 to "الفريق الأزرق"
)

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun PlayerJoinScreenPreview() {
    FeudPartyTheme {
        Box { PlayerJoinScreen(onJoinConfirmed = { _, _ -> }) }
    }
}
