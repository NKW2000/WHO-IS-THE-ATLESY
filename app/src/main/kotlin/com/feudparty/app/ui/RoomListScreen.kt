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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.BrandBadge
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.components.PrimaryButton
import com.feudparty.app.ui.components.Pill
import com.feudparty.app.ui.components.GoldDivider
import com.feudparty.app.ui.components.SecondaryButton
import com.feudparty.app.ui.components.StageBackground
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudShape
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
    if (isPortrait()) {
        PortraitRoomList(playerName, rooms, onPick, onBack)
        return
    }

    StageBackground(contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrandBadge(em = 40.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "أي غرفة؟",
                        color = FeudColors.gold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "أهلاً $playerName — اختار الغرفة اللي بدك تفوت فيها",
                        color = FeudColors.textMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Pill(
                    text = if (rooms.isEmpty()) "عم ندوّر..." else "${rooms.size.ar()} غرفة قريبة",
                    color = if (rooms.isEmpty()) FeudColors.stageAlt else FeudColors.lime,
                    textColor = if (rooms.isEmpty()) FeudColors.textMuted else FeudColors.ink
                )
                Spacer(Modifier.width(12.dp))
                SecondaryButton(
                    text = "رجوع",
                    onClick = onBack,
                    accent = FeudColors.teal,
                    modifier = Modifier.width(140.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            GoldDivider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))

            if (rooms.isEmpty()) {
                SearchingState(modifier = Modifier.fillMaxSize())
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
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

/** لستة الغرف بالوضع الطولي — نفس كرت التصميم. */
@Composable
private fun PortraitRoomList(
    playerName: String,
    rooms: List<PlayerViewModel.Room>,
    onPick: (PlayerViewModel.Room) -> Unit,
    onBack: () -> Unit
) {
    StageBackground(contentPadding = PaddingValues(horizontal = 22.dp, vertical = 18.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "أي غرفة؟",
                        color = FeudColors.gold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "أهلاً $playerName — اختار الغرفة اللي بدك تفوت فيها",
                        color = FeudColors.textMuted,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Pill(
                    text = if (rooms.isEmpty()) "عم ندوّر" else "${rooms.size.ar()} غرف",
                    color = if (rooms.isEmpty()) FeudColors.stageAlt else FeudColors.lime,
                    textColor = if (rooms.isEmpty()) FeudColors.textMuted else FeudColors.ink
                )
            }

            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(FeudColors.ink, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.height(12.dp))

            if (rooms.isEmpty()) {
                SearchingState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(rooms, key = { it.endpointId }) { room ->
                        RoomRow(room = room, onClick = { onPick(room) })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = "رجوع",
                onClick = onBack,
                color = FeudColors.teal,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RoomRow(room: PlayerViewModel.Room, onClick: () -> Unit) {
    CartoonSurface(
        modifier = Modifier.fillMaxWidth(),
        color = FeudColors.stageAlt,
        borderWidth = 3.dp,
        corner = FeudShape.block,
        shadow = 5.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandBadge(em = 30.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    room.name,
                    color = FeudColors.text,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "غرفة قريبة · جاهزة",
                    color = FeudColors.textMuted,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Pill(text = "فوت", color = FeudColors.lime, textColor = FeudColors.ink)
        }
    }
}

/** حالة البحث: نقط بتنبض وسطر بيقول شو لازم يصير. */
@Composable
private fun SearchingState(modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "searching")
    val scale by pulse.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(760), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(FeudColors.gold, FeudColors.teal, FeudColors.pink)
                    .forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .scale(if (index % 2 == 0) scale else 2f - scale)
                                .size(14.dp)
                                .background(color, CircleShape)
                        )
                    }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "عم ندوّر على غرف قريبة",
                color = FeudColors.text,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "خلّي المضيف يفتح اللعبة ويضغط «ابدأ الاستضافة»، وتأكد إنه " +
                    "البلوتوث والواي فاي شغّالين عند الاتنين",
                color = FeudColors.textMuted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
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
