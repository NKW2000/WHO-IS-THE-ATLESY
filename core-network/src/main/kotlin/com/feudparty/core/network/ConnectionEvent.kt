package com.feudparty.core.network

sealed class ConnectionEvent {
    data class EndpointConnected(val endpointId: String) : ConnectionEvent()
    data class EndpointDisconnected(val endpointId: String) : ConnectionEvent()
    data class ClientMessageReceived(val endpointId: String, val message: ClientMessage) : ConnectionEvent()
    data class HostMessageReceived(val message: HostMessage) : ConnectionEvent()
    data class Error(val description: String) : ConnectionEvent()
}
