package com.feudparty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feudparty.app.settings.GameSettings
import com.feudparty.core.game.GameEngine
import com.feudparty.core.game.GameEvent
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Player
import com.feudparty.core.game.Question
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * وضع التجربة — لعبة كاملة على جهاز واحد، بدون شبكة ولا أجهزة تانية.
 *
 * نفس محرك اللعبة الحقيقي بالضبط؛ الفرق الوحيد إنه بدل ما كل لاعب يضغط
 * على جهازه، بتضغط إنت على مربّعه بالشاشة. هيك بتشوف لوح المضيف وكل
 * شاشات اللاعبين مع بعض وبتجرّب القواعد كلها.
 */
class DemoViewModel(
    questions: List<Question>,
    fastMoneyQuestions: List<Question>,
    settings: GameSettings,
    playerNames: List<Pair<String, TeamId>> = DEFAULT_PLAYERS,
    private val tickMillis: Long = 1_000L
) : ViewModel() {

    private val engine = GameEngine(
        GameState(
            questions = questions,
            fastMoneyQuestions = fastMoneyQuestions,
            multipliers = settings.multipliersForRounds(),
            strikesToSteal = settings.strikesToSteal,
            fastMoneyTarget = settings.fastMoneyTarget,
            fastMoneyFirstSeconds = settings.fastMoneyFirstSeconds,
            fastMoneySecondSeconds = settings.fastMoneySecondSeconds,
            players = playerNames.mapIndexed { index, (name, team) ->
                Player(id = "demo-$index", name = name, teamId = team)
            },
            teams = mapOf(
                TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "الفريق الأحمر", connected = true),
                TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الفريق الأزرق", connected = true)
            )
        )
    )

    private val _state = MutableStateFlow(engine.state)
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun buzz(playerId: String) = apply(GameEvent.Buzz(playerId, System.currentTimeMillis()))

    fun judgeCorrect(answerIndex: Int) = apply(GameEvent.JudgeCorrect(answerIndex))

    fun judgeWrong() = apply(GameEvent.JudgeWrong)

    fun nextRound() = apply(GameEvent.NextRound)

    fun endGame() {
        stopTimer()
        apply(GameEvent.EndGame)
    }

    fun startFastMoneyTimer() {
        apply(GameEvent.FastMoneyStartTimer)
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (engine.state.fastMoney?.timerRunning == true) {
                delay(tickMillis)
                apply(GameEvent.FastMoneyTick)
            }
        }
    }

    fun submitFastMoneyAnswer(answerIndex: Int?) {
        apply(GameEvent.FastMoneySubmit(answerIndex))
        if (engine.state.fastMoney?.timerRunning != true) stopTimer()
    }

    fun revealFastMoney() {
        stopTimer()
        apply(GameEvent.FastMoneyReveal)
    }

    private fun apply(event: GameEvent) {
        _state.value = engine.apply(event)
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

    companion object {
        /** فريقين × لاعبين — أقل عدد بيبيّن دورة الأدوار كاملة. */
        val DEFAULT_PLAYERS = listOf(
            "سامر" to TeamId.TEAM_1,
            "هناء" to TeamId.TEAM_1,
            "ليلى" to TeamId.TEAM_2,
            "زيد" to TeamId.TEAM_2
        )
    }
}
