package com.feudparty.app.viewmodel

import com.feudparty.core.network.ClientMessage
import com.feudparty.core.network.ConnectionEvent
import com.feudparty.core.network.HostMessage
import com.feudparty.core.network.NearbyConnectionsManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** بديل بالذاكرة عن Nearby — بيخلينا نختبر الـ ViewModels بدون جهاز. */
class FakeNearbyConnectionsManager : NearbyConnectionsManager {

    private val _events = MutableSharedFlow<ConnectionEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<ConnectionEvent> = _events.asSharedFlow()

    var advertisingAs: String? = null
        private set
    var advertisedName: String? = null
        private set
    val connectRequests = mutableListOf<String>()
    var discoveringAs: String? = null
        private set
    var stopped = false
        private set

    val broadcasts = mutableListOf<HostMessage>()
    val directHostMessages = mutableListOf<Pair<String, HostMessage>>()
    val clientMessages = mutableListOf<Pair<String, ClientMessage>>()

    override fun startAdvertising(serviceName: String, displayName: String?) {
        advertisingAs = serviceName
        advertisedName = displayName
    }

    override fun startDiscovery(serviceName: String) {
        discoveringAs = serviceName
    }

    override fun connectTo(endpointId: String) {
        connectRequests += endpointId
    }

    override fun sendToEndpoint(endpointId: String, message: ClientMessage) {
        clientMessages += endpointId to message
    }

    override fun sendToEndpoint(endpointId: String, message: HostMessage) {
        directHostMessages += endpointId to message
    }

    override fun broadcastToAll(message: HostMessage) {
        broadcasts += message
    }

    override fun stop() {
        stopped = true
    }

    /** بتحاكي وصول حدث من الشبكة. */
    fun emit(event: ConnectionEvent) {
        check(_events.tryEmit(event)) { "امتلأ بفر الأحداث بالاختبار" }
    }
}
