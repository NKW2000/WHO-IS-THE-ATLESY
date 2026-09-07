package com.feudparty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feudparty.core.game.GameEngine
import com.feudparty.core.game.GameEvent
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState
import com.feudparty.core.game.maskedForPlayers
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
 * جديدة لأجهزة اللاعبين. الأجهزة ما بتحسب ولا بتقرر إشي، وبتوصلها نسخة
 * مقنّعة: بدون نص السؤال وبدون نصوص الأجوبة المخفية.
 */
class HostViewModel(
    private val connections: NearbyConnectionsManager,
    questions: List<Question>,
    multipliers: List<Int> = DEFAULT_MULTIPLIERS,
    strikesToSteal: Int = GameEngine.DEFAULT_STRIKES_TO_STEAL,
    private val serviceName: String = SERVICE_NAME,
    private val tickMillis: Long = 1_000L
) : ViewModel() {

    private val engine = GameEngine(
        GameState(
            questions = questions,
            multipliers = multipliers,
            strikesToSteal = strikesToSteal,
            teams = mapOf(
                TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "الفريق الأحمر"),
                TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الفريق الأزرق")
            )
        )
    )

    private val _uiState = MutableStateFlow(engine.state)
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _advertising = MutableStateFlow(false)
    val advertising: StateFlow<Boolean> = _advertising.asStateFlow()

    /** كل جهاز متصل = لاعب واحد؛ منستعمل معرّف الاتصال كمعرّف اللاعب. */
    private val knownEndpoints = mutableSetOf<String>()

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

    /** بعد ما تبلّش اللعبة ما بيضل حدا يغيّر فريقه. */
    private var started = false

    /** عدّاد الثواني بيمشي بجهاز المضيف بس. */
    private var clockJob: Job? = null

    fun startGame() {
        started = true
        startClock()
    }

    /** المضيف بينقل لاعب لفريق تاني — بس قبل ما تبلّش اللعبة. */
    fun movePlayer(playerId: String, teamId: TeamId) {
        if (started) return
        applyAndBroadcast(GameEvent.PlayerMoved(playerId, teamId))
        connections.sendToEndpoint(playerId, HostMessage.Assigned(playerId, teamId))
    }

    fun judgeCorrect(answerIndex: Int) = applyAndBroadcast(GameEvent.JudgeCorrect(answerIndex))

    fun judgeWrong() = applyAndBroadcast(GameEvent.JudgeWrong)

    fun nextRound() = applyAndBroadcast(GameEvent.NextRound)

    fun endGame() {
        clockJob?.cancel()
        clockJob = null
        applyAndBroadcast(GameEvent.EndGame)
    }

    /** عدّاد الثواني — بيمشي بجهاز المضيف وبينبثّ للكل مع الحالة. */
    private fun startClock() {
        if (clockJob?.isActive == true) return
        clockJob = viewModelScope.launch {
            while (true) {
                delay(tickMillis)
                val current = engine.state
                if (current.gameOver) break
                if (current.answerSecondsLeft > 0 || current.choiceSecondsLeft > 0) {
                    applyAndBroadcast(GameEvent.Tick)
                }
            }
        }
    }

    fun dismissError() {
        _lastError.value = null
    }

    private fun handleClientMessage(endpointId: String, message: ClientMessage) {
        when (message) {
            is ClientMessage.Join -> addPlayer(endpointId, message.playerName, message.teamId)

            is ClientMessage.ChangeTeam -> {
                // تغيير الفريق مسموح بس قبل ما تبلّش اللعبة.
                if (endpointId in knownEndpoints && !started) {
                    applyAndBroadcast(GameEvent.PlayerMoved(endpointId, message.teamId))
                    connections.sendToEndpoint(
                        endpointId,
                        HostMessage.Assigned(endpointId, message.teamId)
                    )
                }
            }
            is ClientMessage.Buzz -> {
                // منعتمد على معرّف الجهاز، مش على اللي الجهاز بيدّعيه.
                if (endpointId !in knownEndpoints) return
                applyAndBroadcast(GameEvent.Buzz(endpointId, message.atMillis))
            }

            is ClientMessage.Choose -> {
                // بس اللاعب اللي المحرك فاتح له القرار بينسمع منه.
                if (endpointId !in engine.state.armedPlayerIds()) return
                applyAndBroadcast(GameEvent.ChooseControl(message.play))
            }
        }
    }

    private fun addPlayer(endpointId: String, name: String, wanted: TeamId? = null) {
        val existing = engine.state.player(endpointId)
        // اللاعب بيختار فريقه؛ إذا ما اختار منحطه بالفريق الأقل عدداً.
        val teamId = wanted ?: existing?.teamId ?: smallerTeam()
        knownEndpoints += endpointId
        connections.sendToEndpoint(endpointId, HostMessage.Assigned(endpointId, teamId))
        applyAndBroadcast(GameEvent.PlayerJoined(endpointId, name, teamId))
    }

    /** اللاعب الجديد بيروح للفريق الأقل عدداً حتى تضل الفرق متوازنة. */
    private fun smallerTeam(): TeamId {
        val one = engine.state.playersOf(TeamId.TEAM_1).size
        val two = engine.state.playersOf(TeamId.TEAM_2).size
        return if (two < one) TeamId.TEAM_2 else TeamId.TEAM_1
    }

    private fun handleDisconnect(endpointId: String) {
        if (endpointId !in knownEndpoints) return
        knownEndpoints -= endpointId
        applyAndBroadcast(GameEvent.PlayerLeft(endpointId))
    }

    private fun applyAndBroadcast(event: GameEvent) {
        val newState = engine.apply(event)
        _uiState.value = newState
        connections.broadcastToAll(HostMessage.StateUpdate(newState.maskedForPlayers()))
    }

    override fun onCleared() {
        super.onCleared()
        clockJob?.cancel()
        connections.stop()
    }

    companion object {
        const val SERVICE_NAME = "feud-party"

        /** مضاعفات جولات البرنامج: عادي، عادي، ×٢، ×٣ وبعدها بتضل ×٣. */
        val DEFAULT_MULTIPLIERS = listOf(1, 1, 2, 3)

        /** أقل عدد لاعبين لكل فريق حتى تبلّش اللعبة. */
        const val MIN_PLAYERS_PER_TEAM = 1
    }
}
