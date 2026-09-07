package com.feudparty.app.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feudparty.app.settings.GameSettings
import com.feudparty.app.ui.BankSettingsScreen
import com.feudparty.app.ui.GameOverScreen
import com.feudparty.app.ui.HomeScreen
import com.feudparty.app.ui.HostGameBoardScreen
import com.feudparty.app.ui.HostSettingsScreen
import com.feudparty.app.ui.HostSetupScreen
import com.feudparty.app.ui.IntroScreen
import com.feudparty.app.ui.RoomListScreen
import com.feudparty.app.ui.PlayerJoinScreen
import com.feudparty.app.ui.PlayerScreen
import com.feudparty.app.ui.ScoreboardScreen
import com.feudparty.app.ui.components.CartoonSurface
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.viewmodel.HostViewModel
import com.feudparty.app.viewmodel.PlayerViewModel
import com.feudparty.core.game.PlayerMark
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId

/**
 * معرض الشاشات — نسخة الديمو. بيعرض كل شاشة باللعبة بحالة مزيّفة، فبتقدر
 * تتفرّج عليهن وتعدّل بالتصميم بجهاز واحد، بدون مضيف ولا لاعبين ولا شبكة.
 *
 * الشاشات شغّالة فعلياً (أزرارها بتضغط)، بس ما ورا أي زر منطق لعبة —
 * التنقّل بينهن من الشريط اللي تحت.
 */
private class DemoScreen(val title: String, val content: @Composable () -> Unit)

@Composable
fun DemoGallery(modifier: Modifier = Modifier) {
    var index by remember { mutableIntStateOf(0) }
    var settings by remember { mutableStateOf(GameSettings()) }
    val screens = remember(settings) { demoScreens(settings) { settings = it } }
    val current = screens[index.coerceIn(screens.indices)]

    Box(modifier = modifier.fillMaxSize()) {
        current.content()

        // شريط التنقّل: بيطفو فوق الشاشة، وبيضل صغير حتى ما يغطّيها.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NavKey(label = "‹") { index = (index + screens.size - 1) % screens.size }
            CartoonSurface(
                color = FeudColors.ink.copy(alpha = 0.92f),
                borderWidth = 2.dp,
                corner = 10.dp,
                shadow = 3.dp
            ) {
                Text(
                    "${index + 1}/${screens.size} · ${current.title}",
                    color = FeudColors.gold,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(230.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            NavKey(label = "›") { index = (index + 1) % screens.size }
        }
    }
}

@Composable
private fun NavKey(label: String, onClick: () -> Unit) {
    CartoonSurface(
        color = FeudColors.gold,
        borderWidth = 2.dp,
        corner = 10.dp,
        shadow = 3.dp,
        onClick = onClick
    ) {
        Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
            Text(label, color = FeudColors.ink, style = MaterialTheme.typography.titleLarge)
        }
    }
}

private fun demoScreens(
    settings: GameSettings,
    onSettings: (GameSettings) -> Unit
): List<DemoScreen> = listOf(
    DemoScreen("المقدمة") { IntroScreen(onDone = {}) },
    DemoScreen("الرئيسية") {
        HomeScreen(onHostClick = {}, onJoinClick = {}, onSettingsClick = {})
    },
    DemoScreen("الإعدادات العامة") {
        BankSettingsScreen(
            settings = settings,
            bankMessage = null,
            bankMessageIsError = false,
            onImportBank = { _, _ -> },
            onClearBank = {},
            onBack = {}
        )
    },
    DemoScreen("إعدادات المضيف") {
        HostSettingsScreen(
            settings = settings,
            onSettingsChange = onSettings,
            onBack = {},
            onContinue = {}
        )
    },
    DemoScreen("لوبي المضيف") {
        HostSetupScreen(
            teams = demoState().teams,
            players = demoPlayers,
            advertising = true,
            minPerTeam = HostViewModel.MIN_PLAYERS_PER_TEAM,
            onStartHosting = {},
            onMovePlayer = { _, _ -> },
            onBeginGame = {}
        )
    },
    DemoScreen("لوح المضيف — مواجهة") {
        HostGameBoardScreen(
            state = demoState(RoundPhase.FACE_OFF, revealed = 0, strikes = 0),
            onCorrect = {},
            onWrong = {},
            onNextRound = {}
        )
    },
    DemoScreen("لوح المضيف — لعب") {
        HostGameBoardScreen(demoState(), onCorrect = {}, onWrong = {}, onNextRound = {})
    },
    DemoScreen("لوح المضيف — نهاية الجولة") {
        HostGameBoardScreen(
            state = demoState(RoundPhase.ROUND_END, revealed = 6),
            onCorrect = {},
            onWrong = {},
            onNextRound = {}
        )
    },
    DemoScreen("النتيجة بين الجولات") {
        ScoreboardScreen(demoState(RoundPhase.SCOREBOARD, revealed = 6), onContinue = {})
    },
    DemoScreen("نهاية اللعبة") {
        GameOverScreen(
            state = demoState(RoundPhase.GAME_OVER, revealed = 6, gameOver = true),
            onBackHome = {},
            onBackToLobby = {}
        )
    },
    DemoScreen("انضمام لاعب") { PlayerJoinScreen(onJoinConfirmed = {}) },
    DemoScreen("لستة الغرف") {
        RoomListScreen(
            playerName = "عبد الرحمن",
            rooms = listOf(
                PlayerViewModel.Room("a", "غرفة العيلة"),
                PlayerViewModel.Room("b", "سهرة الجمعة"),
                PlayerViewModel.Room("c", "غرفة الشباب")
            ),
            onPick = {},
            onBack = {}
        )
    },
    DemoScreen("لوبي اللاعب") {
        PlayerScreen(
            state = demoState(matchStarted = false, revealed = 0),
            playerId = "a1",
            teamId = TeamId.TEAM_1,
            mark = PlayerMark.IDLE,
            status = PlayerViewModel.ConnectionStatus.CONNECTED,
            onBuzz = {}
        )
    },
    DemoScreen("زر اللاعب") {
        PlayerScreen(
            state = demoState(RoundPhase.FACE_OFF, revealed = 0, strikes = 0, maskQuestion = true),
            playerId = "a1",
            teamId = TeamId.TEAM_1,
            mark = PlayerMark.ARMED,
            status = PlayerViewModel.ConnectionStatus.CONNECTED,
            onBuzz = {}
        )
    },
    DemoScreen("لوح اللاعب") {
        PlayerScreen(
            state = demoState(maskQuestion = true),
            playerId = "a2",
            teamId = TeamId.TEAM_1,
            mark = PlayerMark.ARMED,
            status = PlayerViewModel.ConnectionStatus.CONNECTED,
            onBuzz = {}
        )
    },
    DemoScreen("اللاعب — غلط") {
        PlayerScreen(
            state = demoState(maskQuestion = true, strikes = 3),
            playerId = "a2",
            teamId = TeamId.TEAM_1,
            mark = PlayerMark.WRONG,
            status = PlayerViewModel.ConnectionStatus.CONNECTED,
            onBuzz = {}
        )
    },
    DemoScreen("يلعب أو يمرّر") {
        PlayerScreen(
            state = demoState(RoundPhase.PLAY_OR_PASS, revealed = 1, maskQuestion = true),
            playerId = "a1",
            teamId = TeamId.TEAM_1,
            mark = PlayerMark.ARMED,
            status = PlayerViewModel.ConnectionStatus.CONNECTED,
            onBuzz = {},
            onChoose = {}
        )
    }
)
