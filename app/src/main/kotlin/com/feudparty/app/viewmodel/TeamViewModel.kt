package com.feudparty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feudparty.core.game.BuzzState
import com.feudparty.core.game.GameState
import com.feudparty.core.game.TeamId
import com.feudparty.core.network.ClientMessage
import com.feudparty.core.network.ConnectionEvent
import com.feudparty.core.network.HostMessage
import com.feudparty.core.network.NearbyConnectionsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * جهاز الفريق — عميل "غبي": بيبعت أحداث للمضيف وبيعرض الحالة اللي بتوصله،
 * وما بيحسب نقاط ولا بيقرر مين بزّ أول.
 */
class TeamViewModel(
    private val connections: NearbyConnectionsManager,
    private val serviceName: String = HostViewModel.SERVICE_NAME,
    private val clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    enum class ConnectionStatus { IDLE, SEARCHING, CONNECTED, DISCONNECTED }

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    /** الفريق اللي خصصه المضيف لهاد الجهاز — بيوصل بـ [HostMessage.Assigned]. */
    private val _assignedTeam = MutableStateFlow<TeamId?>(null)
    val assignedTeam: StateFlow<TeamId?> = _assignedTeam.asStateFlow()

    private val _status = MutableStateFlow(ConnectionStatus.IDLE)
    val status: StateFlow<ConnectionStatus> = _status.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private var hostEndpointId: String? = null

    /** الاسم بينحفظ لحد ما يصير في اتصال، لأن اللاعب بيكتبه قبل ما نلاقي المضيف. */
    private var pendingTeamName: String? = null

    init {
        viewModelScope.launch {
            connections.events.collect { event ->
                when (event) {
                    is ConnectionEvent.EndpointConnected -> onConnected(event.endpointId)
                    is ConnectionEvent.EndpointDisconnected -> onDisconnected(event.endpointId)
                    is ConnectionEvent.HostMessageReceived -> handleHostMessage(event.message)
                    is ConnectionEvent.Error -> _lastError.value = event.description
                    else -> Unit
                }
            }
        }
    }

    /** بيبلّش البحث عن المضيف وبيسجّل اسم الفريق لبعتو أول ما نتصل. */
    fun join(teamName: String) {
        pendingTeamName = teamName
        if (_status.value != ConnectionStatus.SEARCHING) {
            _status.value = ConnectionStatus.SEARCHING
            connections.startDiscovery(serviceName)
        }
        flushPendingName()
    }

    fun onBuzzTapped() {
        val endpointId = hostEndpointId ?: return
        val teamId = _assignedTeam.value ?: return
        if (_gameState.value?.buzzState != BuzzState.OPEN) return
        connections.sendToEndpoint(endpointId, ClientMessage.Buzz(teamId, clock()))
    }

    /** الزر بيشتغل بس إذا اللعبة فاتحة وما حدا سبقنا. */
    fun canBuzz(): Boolean =
        _status.value == ConnectionStatus.CONNECTED &&
            _assignedTeam.value != null &&
            _gameState.value?.buzzState == BuzzState.OPEN

    fun dismissError() {
        _lastError.value = null
    }

    private fun onConnected(endpointId: String) {
        hostEndpointId = endpointId
        _status.value = ConnectionStatus.CONNECTED
        flushPendingName()
    }

    private fun onDisconnected(endpointId: String) {
        if (endpointId != hostEndpointId) return
        hostEndpointId = null
        _status.value = ConnectionStatus.DISCONNECTED
    }

    private fun flushPendingName() {
        val endpointId = hostEndpointId ?: return
        val name = pendingTeamName ?: return
        connections.sendToEndpoint(endpointId, ClientMessage.Join(name))
    }

    private fun handleHostMessage(message: HostMessage) {
        when (message) {
            is HostMessage.StateUpdate -> _gameState.value = message.state
            is HostMessage.Assigned -> _assignedTeam.value = message.teamId
        }
    }

    override fun onCleared() {
        super.onCleared()
        connections.stop()
    }
}
