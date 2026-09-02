package com.feudparty.app.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.feudparty.app.ui.FastMoneyHostScreen
import com.feudparty.app.ui.FastMoneyTeamScreen
import com.feudparty.app.ui.GameOverScreen
import com.feudparty.app.ui.HomeScreen
import com.feudparty.app.ui.HostGameBoardScreen
import com.feudparty.app.ui.HostSetupScreen
import com.feudparty.app.ui.TeamBuzzScreen
import com.feudparty.app.ui.TeamJoinScreen
import com.feudparty.app.viewmodel.HostViewModel
import com.feudparty.app.viewmodel.TeamViewModel
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.network.NearbyConnectionsManagerImpl
import com.feudparty.data.questions.QuestionBank

object Routes {
    const val HOME = "home"
    const val HOST_SETUP = "host_setup"
    const val HOST_BOARD = "host_board"
    const val HOST_RESULT = "host_result"
    const val TEAM_JOIN = "team_join"
    const val TEAM_BUZZ = "team_buzz"
}

/** عدد جولات اللوح قبل الجولة السريعة — نفس عددها بالبرنامج. */
private const val ROUNDS_PER_GAME = 4

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
        containerColor = FeudColors.deepNavy
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onHostClick = { navController.navigate(Routes.HOST_SETUP) },
                    onJoinClick = { navController.navigate(Routes.TEAM_JOIN) }
                )
            }

            composable(Routes.HOST_SETUP) {
                val vm = hostViewModel(activityOwner, context)
                val state by vm.uiState.collectAsStateWithLifecycle()
                val advertising by vm.advertising.collectAsStateWithLifecycle()
                ErrorSnackbar(vm.lastError.collectAsStateWithLifecycle().value, snackbarHostState, vm::dismissError)

                HostSetupScreen(
                    teams = state.teams.values.sortedBy { it.id },
                    advertising = advertising,
                    onStartHosting = vm::startHosting,
                    onBeginGame = { navController.navigate(Routes.HOST_BOARD) }
                )
            }

            composable(Routes.HOST_BOARD) {
                val vm = hostViewModel(activityOwner, context)
                val state by vm.uiState.collectAsStateWithLifecycle()
                ErrorSnackbar(vm.lastError.collectAsStateWithLifecycle().value, snackbarHostState, vm::dismissError)

                LaunchedEffect(state.gameOver) {
                    if (state.gameOver) {
                        navController.navigate(Routes.HOST_RESULT) {
                            popUpTo(Routes.HOST_BOARD) { inclusive = true }
                        }
                    }
                }

                if (state.phase == RoundPhase.FAST_MONEY) {
                    FastMoneyHostScreen(
                        state = state,
                        onStartTimer = vm::startFastMoneyTimer,
                        onSubmit = vm::submitFastMoneyAnswer,
                        onReveal = vm::revealFastMoney,
                        onEndGame = vm::endGame
                    )
                } else {
                    HostGameBoardScreen(
                        state = state,
                        onCorrect = vm::judgeCorrect,
                        onWrong = vm::judgeWrong,
                        onNextRound = vm::nextRound
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

            composable(Routes.TEAM_JOIN) {
                val vm = teamViewModel(activityOwner, context)
                ErrorSnackbar(vm.lastError.collectAsStateWithLifecycle().value, snackbarHostState, vm::dismissError)

                TeamJoinScreen(onJoinConfirmed = { name ->
                    vm.join(name)
                    navController.navigate(Routes.TEAM_BUZZ)
                })
            }

            composable(Routes.TEAM_BUZZ) {
                val vm = teamViewModel(activityOwner, context)
                val state by vm.gameState.collectAsStateWithLifecycle()
                val myTeam by vm.assignedTeam.collectAsStateWithLifecycle()
                val status by vm.status.collectAsStateWithLifecycle()
                ErrorSnackbar(vm.lastError.collectAsStateWithLifecycle().value, snackbarHostState, vm::dismissError)

                val live = state
                when {
                    live != null && live.gameOver -> GameOverScreen(state = live)
                    live != null && live.phase == RoundPhase.FAST_MONEY ->
                        FastMoneyTeamScreen(state = live, myTeam = myTeam)

                    else -> TeamBuzzScreen(
                        state = state,
                        myTeam = myTeam,
                        status = status,
                        canBuzz = vm.canBuzz(),
                        onBuzz = vm::onBuzzTapped
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
            val game = QuestionBank.randomGame(rounds = ROUNDS_PER_GAME)
            HostViewModel(
                connections = NearbyConnectionsManagerImpl(context.applicationContext, "مضيف"),
                questions = game.rounds,
                fastMoneyQuestions = game.fastMoney
            )
        }
    })

@Composable
private fun teamViewModel(owner: ViewModelStoreOwner, context: Context): TeamViewModel =
    viewModel(viewModelStoreOwner = owner, factory = viewModelFactory {
        initializer {
            TeamViewModel(NearbyConnectionsManagerImpl(context.applicationContext, "فريق"))
        }
    })
