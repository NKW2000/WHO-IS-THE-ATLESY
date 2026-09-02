package com.feudparty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feudparty.core.game.GameEngine
import com.feudparty.core.game.GameEvent
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState
import com.feudparty.core.game.maskedForTeams
import com.feudparty.core.network.ClientMessage
import com.feudparty.core.network.ConnectionEvent
import com.feudparty.core.network.HostMessage
import com.feudparty.core.network.NearbyConnectionsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * جهاز المضيف — مصدر الحقيقة الوحيد. بيشغّل [GameEngine] وبيبثّ كل حالة
 * جديدة لأجهزة الفرق. أجهزة الفرق ما بتحسب ولا بتقرر إشي، وبتوصلها نسخة
 * مقنّعة من الحالة (بدون نصوص الأجوبة المخفية).
 */
class HostViewModel(
    private val connections: NearbyConnectionsManager,
    questions: List<Question>,
    fastMoneyQuestions: List<Question> = emptyList(),
    multipliers: List<Int> = DEFAULT_MULTIPLIERS,
    private val serviceName: String = SERVICE_NAME,
    private val tickMillis: Long = 1_000L
) : ViewModel() {

    private val engine = GameEngine(
        GameState(
            questions = questions,
            multipliers = multipliers,
            fastMoneyQuestions = fastMoneyQuestions,
            teams = mapOf(
                TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "فريق ١"),
                TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "فريق ٢")
            )
        )
    )

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _advertising = MutableStateFlow(false)
    val advertising: StateFlow<Boolean> = _advertising.asStateFlow()

    /** أي جهاز (endpoint) مربوط بأي فريق — أول ينضم بياخد فريق ١. */
    private val endpointToTeam = mutableMapOf<String, TeamId>()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            connections.events.collect { event ->
                when (event) {
                    is ConnectionEvent.ClientMessageReceived ->
                        handleClientMessage(event.endpointId, event.message)

                    is ConnectionEvent.EndpointDisconnected -> handleDisconnect(event.endpointId)
                    is ConnectionEvent.Error -> _lastError.value = event.description
                    else -> Unit
                }
            }
        }
    }

    fun startHosting() {
        if (_advertising.value) return
        _advertising.value = true
        connections.startAdvertising(serviceName)
    }

    fun judgeCorrect(answerIndex: Int) = applyAndBroadcast(GameEvent.JudgeCorrect(answerIndex))

    fun judgeWrong() = applyAndBroadcast(GameEvent.JudgeWrong)

    fun nextRound() = applyAndBroadcast(GameEvent.NextRound)

    // -------------------------------------------------------- الجولة السريعة

    fun startFastMoneyTimer() {
        applyAndBroadcast(GameEvent.FastMoneyStartTimer)
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (engine.state.fastMoney?.timerRunning == true) {
                delay(tickMillis)
                applyAndBroadcast(GameEvent.FastMoneyTick)
            }
        }
    }

    fun submitFastMoneyAnswer(answerIndex: Int?) {
        applyAndBroadcast(GameEvent.FastMoneySubmit(answerIndex))
        // بين لاعب ولاعب بيوقف الوقت لحد ما المضيف يشغّله من جديد.
        if (engine.state.fastMoney?.timerRunning != true) stopTimer()
    }

    fun revealFastMoney() {
        stopTimer()
        applyAndBroadcast(GameEvent.FastMoneyReveal)
    }

    fun endGame() {
        stopTimer()
        applyAndBroadcast(GameEvent.EndGame)
    }

    fun dismissError() {
        _lastError.value = null
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun handleClientMessage(endpointId: String, message: ClientMessage) {
        when (message) {
            is ClientMessage.Join -> assignTeam(endpointId, message.teamName)
            is ClientMessage.Buzz -> {
                // منعتمد على الفريق المخصص للجهاز، مش على اللي الجهاز بيدّعيه.
                val teamId = endpointToTeam[endpointId] ?: return
                applyAndBroadcast(GameEvent.Buzz(teamId, message.atMillis))
            }
        }
    }

    private fun assignTeam(endpointId: String, teamName: String) {
        val teamId = endpointToTeam[endpointId] ?: firstFreeTeam() ?: run {
            _lastError.value = "اللعبة ممتلئة — بس فريقين بيقدروا ينضموا"
            return
        }
        endpointToTeam[endpointId] = teamId
        connections.sendToEndpoint(endpointId, HostMessage.Assigned(teamId))
        applyAndBroadcast(GameEvent.TeamJoined(teamId, teamName))
    }

    private fun firstFreeTeam(): TeamId? =
        TeamId.entries.firstOrNull { it !in endpointToTeam.values }

    private fun handleDisconnect(endpointId: String) {
        val teamId = endpointToTeam.remove(endpointId) ?: return
        applyAndBroadcast(GameEvent.TeamLeft(teamId))
    }

    private fun applyAndBroadcast(event: GameEvent) {
        val newState = engine.apply(event)
        _uiState.value = newState
        connections.broadcastToAll(HostMessage.StateUpdate(newState.maskedForTeams()))
        if (newState.phase == RoundPhase.GAME_OVER) stopTimer()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        connections.stop()
    }

    companion object {
        const val SERVICE_NAME = "feud-party"

        /** مضاعفات جولات البرنامج: عادي، عادي، ×٢، ×٣ وبعدها بتضل ×٣. */
        val DEFAULT_MULTIPLIERS = listOf(1, 1, 2, 3)
    }
}
