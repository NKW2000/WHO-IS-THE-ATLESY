package com.feudparty.app.navigation

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.feudparty.app.feedback.GameStateCues
import com.feudparty.app.feedback.PlayerMarkCues
import com.feudparty.app.ui.GameOverScreen
import com.feudparty.app.ui.HomeScreen
import com.feudparty.app.ui.HostGameBoardScreen
import com.feudparty.app.ui.HostSettingsScreen
import com.feudparty.app.ui.HostSetupScreen
import com.feudparty.app.ui.PlayerJoinScreen
import com.feudparty.app.ui.PlayerScreen
import com.feudparty.app.ui.ScoreboardScreen
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.viewmodel.HostViewModel
import com.feudparty.app.viewmodel.PlayerViewModel
import com.feudparty.app.viewmodel.SettingsViewModel
import com.feudparty.app.settings.SettingsRepository
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.network.LanConnectionsManager
import com.feudparty.core.network.NearbyConnectionsManager
import com.feudparty.core.network.NearbyConnectionsManagerImpl
import com.feudparty.data.questions.QuestionBank

object Routes {
    const val HOME = "home"
    const val HOST_SETUP = "host_setup"
    const val HOST_BOARD = "host_board"
    const val HOST_RESULT = "host_result"
    const val PLAYER_JOIN = "player_join"
    const val PLAYER_BUZZER = "player_buzzer"
    const val HOST_SETTINGS = "host_settings"
}

@Composable
fun FeudNavGraph(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    // منربط الـ ViewModels بالـ Activity مش بشاشة التنقّل، حتى تضل نفس
    // الجلسة (ونفس اتصال Nearby) مشتركة بين شاشة الإعداد وشاشة اللعب.
    val activityOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "ما في ViewModelStoreOwner"
    }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = FeudColors.deepNavy,
        // اللعبة بتاخد كل الشاشة — ما في أشرطة نظام نحجزلها مكان.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        // كل انتقال شريحة سريعة بنفس اتجاه التصميم: للأمام بيدخل من الجنب،
        // وللورا بيرجع بالعكس.
        val slide = tween<IntOffset>(340)
        val fade = tween<Float>(240)

        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
            enterTransition = { slideInHorizontally(slide) { it } + fadeIn(fade) },
            exitTransition = { slideOutHorizontally(slide) { -it / 4 } + fadeOut(fade) },
            popEnterTransition = { slideInHorizontally(slide) { -it / 4 } + fadeIn(fade) },
            popExitTransition = { slideOutHorizontally(slide) { it } + fadeOut(fade) }
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onHostClick = { navController.navigate(Routes.HOST_SETUP) },
                    onJoinClick = { navController.navigate(Routes.PLAYER_JOIN) },
                    onSettingsClick = { navController.navigate(Routes.HOST_SETTINGS) }
                )
            }

            composable(Routes.HOST_SETTINGS) {
                val vm = settingsViewModel(activityOwner, context)
                val settings by vm.settings.collectAsStateWithLifecycle()
                val message by vm.bankMessage.collectAsStateWithLifecycle()
                val failed by vm.bankFailed.collectAsStateWithLifecycle()

                HostSettingsScreen(
                    settings = settings,
                    bankMessage = message,
                    bankMessageIsError = failed,
                    onSettingsChange = vm::update,
                    onImportBank = vm::importBank,
                    onClearBank = vm::clearBank,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.HOST_SETUP) {
                val vm = hostViewModel(activityOwner, context)
                val state by vm.uiState.collectAsStateWithLifecycle()
                val advertising by vm.advertising.collectAsStateWithLifecycle()
                ErrorSnackbar(
                    vm.lastError.collectAsStateWithLifecycle().value,
                    snackbarHostState,
                    vm::dismissError
                )

                HostSetupScreen(
                    teams = state.teams,
                    players = state.players,
                    advertising = advertising,
                    minPerTeam = HostViewModel.MIN_PLAYERS_PER_TEAM,
                    onStartHosting = vm::startHosting,
                    onBeginGame = { navController.navigate(Routes.HOST_BOARD) }
                )
            }

            composable(Routes.HOST_BOARD) {
                val vm = hostViewModel(activityOwner, context)
                val state by vm.uiState.collectAsStateWithLifecycle()
                GameStateCues(state)
                ErrorSnackbar(
                    vm.lastError.collectAsStateWithLifecycle().value,
                    snackbarHostState,
                    vm::dismissError
                )

                LaunchedEffect(state.gameOver) {
                    if (state.gameOver) {
                        navController.navigate(Routes.HOST_RESULT) {
                            popUpTo(Routes.HOST_BOARD) { inclusive = true }
                        }
                    }
                }

                if (state.phase == RoundPhase.SCOREBOARD) {
                    ScoreboardScreen(state = state, onContinue = vm::nextRound)
                } else {
                    HostGameBoardScreen(
                        state = state,
                        onCorrect = vm::judgeCorrect,
                        onWrong = vm::judgeWrong,
                        onNextRound = vm::nextRound,
                        onChoose = vm::chooseControl
                    )
                }
            }

            composable(Routes.HOST_RESULT) {
                val vm = hostViewModel(activityOwner, context)
                val state by vm.uiState.collectAsStateWithLifecycle()
                GameOverScreen(
                    state = state,
                    onBackHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.PLAYER_JOIN) {
                val vm = playerViewModel(activityOwner, context)
                ErrorSnackbar(
                    vm.lastError.collectAsStateWithLifecycle().value,
                    snackbarHostState,
                    vm::dismissError
                )

                PlayerJoinScreen(onJoinConfirmed = { name ->
                    vm.join(name)
                    navController.navigate(Routes.PLAYER_BUZZER)
                })
            }

            composable(Routes.PLAYER_BUZZER) {
                val vm = playerViewModel(activityOwner, context)
                val state by vm.gameState.collectAsStateWithLifecycle()
                val playerId by vm.playerId.collectAsStateWithLifecycle()
                val teamId by vm.teamId.collectAsStateWithLifecycle()
                val status by vm.status.collectAsStateWithLifecycle()
                ErrorSnackbar(
                    vm.lastError.collectAsStateWithLifecycle().value,
                    snackbarHostState,
                    vm::dismissError
                )

                val live = state
                val mark = vm.mark()
                GameStateCues(live)
                PlayerMarkCues(mark)
                when {
                    live != null && live.gameOver -> GameOverScreen(state = live)
                    live != null && live.phase == RoundPhase.SCOREBOARD ->
                        ScoreboardScreen(state = live)
                    else -> PlayerScreen(
                        state = live,
                        playerId = playerId,
                        teamId = teamId,
                        mark = mark,
                        status = status,
                        onBuzz = vm::onBuzzTapped,
                        onChoose = vm::choose
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorSnackbar(
    message: String?,
    hostState: SnackbarHostState,
    onShown: () -> Unit
) {
    LaunchedEffect(message) {
        if (message != null) {
            hostState.showSnackbar(message)
            onShown()
        }
    }
}

@Composable
private fun hostViewModel(owner: ViewModelStoreOwner, context: Context): HostViewModel =
    viewModel(viewModelStoreOwner = owner, factory = viewModelFactory {
        initializer {
            val settings = SettingsRepository(context).load()
            val bank = SettingsRepository(context).questions()
            val questions = QuestionBank.randomGame(rounds = settings.rounds, source = bank)
            HostViewModel(
                connections = connections(context, settings.lanTesting, settings.lanHost, "مضيف"),
                questions = questions,
                multipliers = settings.multipliersForRounds(),
                strikesToSteal = settings.strikesToSteal
            )
        }
    })

/**
 * وضع الاختبار بيستبدل Nearby بوصل TCP، لأن Nearby ما بيشتغل عالمحاكي.
 */
private fun connections(
    context: Context,
    lanTesting: Boolean,
    lanHost: String,
    deviceName: String
): NearbyConnectionsManager = if (lanTesting) {
    LanConnectionsManager(hostAddress = lanHost)
} else {
    NearbyConnectionsManagerImpl(context.applicationContext, deviceName)
}

@Composable
private fun settingsViewModel(owner: ViewModelStoreOwner, context: Context): SettingsViewModel =
    viewModel(viewModelStoreOwner = owner, factory = viewModelFactory {
        initializer { SettingsViewModel(SettingsRepository(context)) }
    })

@Composable
private fun playerViewModel(owner: ViewModelStoreOwner, context: Context): PlayerViewModel =
    viewModel(viewModelStoreOwner = owner, factory = viewModelFactory {
        initializer {
            val settings = SettingsRepository(context).load()
            PlayerViewModel(
                connections(context, settings.lanTesting, settings.lanHost, "لاعب")
            )
        }
    })
