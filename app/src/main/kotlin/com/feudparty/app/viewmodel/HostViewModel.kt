package com.feudparty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feudparty.core.game.GameEngine
import com.feudparty.core.game.GameEvent
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState
import com.feudparty.core.network.ClientMessage
import com.feudparty.core.network.ConnectionEvent
import com.feudparty.core.network.HostMessage
import com.feudparty.core.network.NearbyConnectionsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * جهاز المضيف — مصدر الحقيقة الوحيد. بيشغّل [GameEngine] وبيبثّ كل حالة
 * جديدة لأجهزة الفرق. أجهزة الفرق ما بتحسب ولا بتقرر إشي.
 */
class HostViewModel(
    private val connections: NearbyConnectionsManager,
    questions: List<Question>,
    private val serviceName: String = SERVICE_NAME
) : ViewModel() {

    private val engine = GameEngine(
        GameState(
            questions = questions,
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

    fun nextQuestion() = applyAndBroadcast(GameEvent.NextQuestion)

    fun dismissError() {
        _lastError.value = null
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
        connections.broadcastToAll(HostMessage.StateUpdate(newState))
    }

    override fun onCleared() {
        super.onCleared()
        connections.stop()
    }

    companion object {
        const val SERVICE_NAME = "feud-party"
    }
}
