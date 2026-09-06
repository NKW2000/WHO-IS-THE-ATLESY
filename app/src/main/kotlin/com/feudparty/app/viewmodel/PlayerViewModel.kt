package com.feudparty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feudparty.core.game.GameState
import com.feudparty.core.game.PlayerMark
import com.feudparty.core.game.RoundPhase
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
 * جهاز اللاعب — عميل "غبي": بيبعت ضغطة الزر وبيعرض الحالة اللي بتوصله.
 * ما بيحسب نقاط، ما بيقرر مين ضغط أول، وما بيوصله نص السؤال أصلاً.
 */
class PlayerViewModel(
    private val connections: NearbyConnectionsManager,
    private val serviceName: String = HostViewModel.SERVICE_NAME,
    private val clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    enum class ConnectionStatus { IDLE, SEARCHING, CONNECTED, DISCONNECTED }

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    /** المعرّف اللي خصصه المضيف لهاد الجهاز — بيوصل بـ [HostMessage.Assigned]. */
    private val _playerId = MutableStateFlow<String?>(null)
    val playerId: StateFlow<String?> = _playerId.asStateFlow()

    private val _teamId = MutableStateFlow<TeamId?>(null)
    val teamId: StateFlow<TeamId?> = _teamId.asStateFlow()

    private val _status = MutableStateFlow(ConnectionStatus.IDLE)
    val status: StateFlow<ConnectionStatus> = _status.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private var hostEndpointId: String? = null

    /** الاسم بينحفظ لحد ما يصير في اتصال، لأن اللاعب بيكتبه قبل ما نلاقي المضيف. */
    private var pendingName: String? = null

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

    /** بيبلّش البحث عن المضيف وبيسجّل الاسم لبعتو أول ما نتصل. */
    fun join(name: String) {
        pendingName = name
        if (_status.value != ConnectionStatus.SEARCHING) {
            _status.value = ConnectionStatus.SEARCHING
            connections.startDiscovery(serviceName)
        }
        flushPendingName()
    }

    fun onBuzzTapped() {
        val endpointId = hostEndpointId ?: return
        val id = _playerId.value ?: return
        if (!canBuzz()) return
        connections.sendToEndpoint(endpointId, ClientMessage.Buzz(id, clock()))
    }

    /** قرار «نلعب» أو «نمرّر» — بيوصل بس من اللاعب اللي كسب المواجهة. */
    fun choose(play: Boolean) {
        val endpointId = hostEndpointId ?: return
        val id = _playerId.value ?: return
        if (_gameState.value?.phase != RoundPhase.PLAY_OR_PASS) return
        connections.sendToEndpoint(endpointId, ClientMessage.Choose(id, play))
    }

    /** الزر بيشتغل بس لما المضيف يفتحه لهاد اللاعب بالذات. */
    fun canBuzz(): Boolean {
        val state = _gameState.value ?: return false
        val id = _playerId.value ?: return false
        return _status.value == ConnectionStatus.CONNECTED && id in state.armedPlayerIds()
    }

    /** لون شاشة اللاعب: وميض / أزرق / أخضر / أحمر. */
    fun mark(): PlayerMark = _gameState.value?.markFor(_playerId.value) ?: PlayerMark.IDLE

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
        val name = pendingName ?: return
        connections.sendToEndpoint(endpointId, ClientMessage.Join(name))
    }

    private fun handleHostMessage(message: HostMessage) {
        when (message) {
            is HostMessage.StateUpdate -> _gameState.value = message.state
            is HostMessage.Assigned -> {
                _playerId.value = message.playerId
                _teamId.value = message.teamId
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        connections.stop()
    }
}
