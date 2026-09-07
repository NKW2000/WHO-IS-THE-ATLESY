package com.feudparty.core.network

sealed class ConnectionEvent {
    data class EndpointConnected(val endpointId: String) : ConnectionEvent()
    data class EndpointDisconnected(val endpointId: String) : ConnectionEvent()
    data class ClientMessageReceived(val endpointId: String, val message: ClientMessage) : ConnectionEvent()
    data class HostMessageReceived(val message: HostMessage) : ConnectionEvent()
    /** لقينا غرفة مضيف — اللاعب هو اللي بيختار يفوت فيها. */
    data class RoomFound(val endpointId: String, val name: String) : ConnectionEvent()

    data class RoomLost(val endpointId: String) : ConnectionEvent()

    data class Error(val description: String) : ConnectionEvent()
}
