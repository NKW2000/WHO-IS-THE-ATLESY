package com.feudparty.app.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.GoldDivider
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.app.viewmodel.PlayerViewModel

/**
 * لستة الغرف: كل مضيف قريب بيبيّن باسم غرفته، واللاعب بيدوس على وحدة
 * ليفوت فيها. منضل ندوّر وقت اللستة مفتوحة، فالغرف بتزيد وبتنقص لحالها.
 */
@Composable
fun RoomListScreen(
    playerName: String,
    rooms: List<PlayerViewModel.Room>,
    onPick: (PlayerViewModel.Room) -> Unit,
    onBack: () -> Unit
) {
    StageBackground(contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "أي غرفة؟",
                        color = FeudColors.gold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "أهلاً $playerName — اختار الغرفة اللي بدك تفوت فيها",
                        color = FeudColors.textMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                SecondaryButton(
                    text = "رجوع",
                    onClick = onBack,
                    accent = FeudColors.teal,
                    modifier = Modifier.width(150.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            GoldDivider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))

            if (rooms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "عم ندوّر على غرف قريبة...\nخلّي المضيف يفتح اللعبة ويبلّش البث",
                        color = FeudColors.textSoft,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(rooms, key = { it.endpointId }) { room ->
                        RoomRow(room = room, onClick = { onPick(room) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomRow(room: PlayerViewModel.Room, onClick: () -> Unit) {
    CartoonSurface(
        modifier = Modifier.fillMaxWidth(),
        color = FeudColors.stageAlt,
        borderWidth = 3.dp,
        corner = 14.dp,
        shadow = 5.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                room.name,
                color = FeudColors.text,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                "فوت",
                color = FeudColors.lime,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 880, heightDp = 420)
@Composable
private fun RoomListScreenPreview() {
    FeudPartyTheme {
        RoomListScreen(
            playerName = "عبد الرحمن",
            rooms = listOf(
                PlayerViewModel.Room("a", "غرفة العيلة"),
                PlayerViewModel.Room("b", "غرفة الشباب")
            ),
            onPick = {},
            onBack = {}
        )
    }
}
