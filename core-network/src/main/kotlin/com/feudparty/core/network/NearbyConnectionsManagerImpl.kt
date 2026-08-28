package com.feudparty.core.network

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * التنفيذ الفعلي فوق Nearby Connections API.
 *
 * الجهاز إما مضيف (بيعلن) أو فريق (بيكتشف) — منحدد الدور بأول استدعاء
 * لـ [startAdvertising] أو [startDiscovery]، ومنستخدمو حتى نعرف أي نوع
 * رسالة منتوقع نستقبل، بدل ما نحزر من محتوى البايتات.
 */
class NearbyConnectionsManagerImpl(
    context: Context,
    private val localName: String = "feud-device"
) : NearbyConnectionsManager {

    private enum class Role { NONE, HOST, CLIENT }

    private val client = Nearby.getConnectionsClient(context.applicationContext)
    private val strategy = Strategy.P2P_STAR
    private val connectedEndpoints = mutableSetOf<String>()

    @Volatile
    private var role: Role = Role.NONE

    private val _events = MutableSharedFlow<ConnectionEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    override val events: SharedFlow<ConnectionEvent> = _events.asSharedFlow()

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            when (role) {
                Role.HOST -> runCatching { decodeClientMessage(bytes) }
                    .onSuccess { emit(ConnectionEvent.ClientMessageReceived(endpointId, it)) }
                    .onFailure { emit(ConnectionEvent.Error("رسالة غير مفهومة من $endpointId")) }

                Role.CLIENT -> runCatching { decodeHostMessage(bytes) }
                    .onSuccess { emit(ConnectionEvent.HostMessageReceived(it)) }
                    .onFailure { emit(ConnectionEvent.Error("رسالة غير مفهومة من المضيف")) }

                Role.NONE -> emit(ConnectionEvent.Error("وصلت رسالة قبل تحديد دور الجهاز"))
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            client.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectedEndpoints.add(endpointId)
                // الفريق ما بيحتاج يضل يدوّر بعد ما يلاقي المضيف.
                if (role == Role.CLIENT) client.stopDiscovery()
                emit(ConnectionEvent.EndpointConnected(endpointId))
            } else {
                emit(ConnectionEvent.Error("فشل الاتصال مع $endpointId"))
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedEndpoints.remove(endpointId)
            emit(ConnectionEvent.EndpointDisconnected(endpointId))
        }
    }

    override fun startAdvertising(serviceName: String) {
        role = Role.HOST
        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
        client.startAdvertising(localName, serviceName, connectionLifecycleCallback, options)
            .addOnFailureListener { emit(ConnectionEvent.Error("تعذّر بدء البث: ${it.message}")) }
    }

    override fun startDiscovery(serviceName: String) {
        role = Role.CLIENT
        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
        val endpointCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                client.requestConnection(localName, endpointId, connectionLifecycleCallback)
                    .addOnFailureListener {
                        emit(ConnectionEvent.Error("تعذّر طلب الاتصال: ${it.message}"))
                    }
            }

            override fun onEndpointLost(endpointId: String) = Unit
        }
        client.startDiscovery(serviceName, endpointCallback, options)
            .addOnFailureListener { emit(ConnectionEvent.Error("تعذّر البحث عن اللعبة: ${it.message}")) }
    }

    override fun sendToEndpoint(endpointId: String, message: ClientMessage) {
        send(endpointId, encodeClientMessage(message))
    }

    override fun sendToEndpoint(endpointId: String, message: HostMessage) {
        send(endpointId, encodeHostMessage(message))
    }

    override fun broadcastToAll(message: HostMessage) {
        val bytes = encodeHostMessage(message)
        connectedEndpoints.toList().forEach { id -> send(id, bytes) }
    }

    override fun stop() {
        client.stopAdvertising()
        client.stopDiscovery()
        client.stopAllEndpoints()
        connectedEndpoints.clear()
        role = Role.NONE
    }

    private fun send(endpointId: String, bytes: ByteArray) {
        client.sendPayload(endpointId, Payload.fromBytes(bytes))
            .addOnFailureListener { emit(ConnectionEvent.Error("تعذّر إرسال الرسالة: ${it.message}")) }
    }

    /**
     * ينادى من ثريدات Nearby — لهيك [tryEmit] (ما بتعلّق الثريد) بدل
     * `runBlocking { emit(...) }` اللي بيقفل ثريد الكولباك.
     */
    private fun emit(event: ConnectionEvent) {
        if (!_events.tryEmit(event)) {
            Log.w(TAG, "امتلأ بفر الأحداث، انرمى: $event")
        }
    }

    private companion object {
        const val TAG = "NearbyConnections"
    }
}
