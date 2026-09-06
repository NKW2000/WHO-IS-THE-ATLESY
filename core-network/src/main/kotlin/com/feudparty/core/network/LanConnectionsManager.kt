package com.feudparty.core.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * وصل عبر الشبكة بدل Nearby — **للاختبار بس**.
 *
 * Nearby Connections بيحتاج بلوتوث وواي فاي حقيقيين، فما بيشتغل على
 * المحاكي. هاي النسخة بتستعمل TCP عادي، فبتقدر تشغّل ٣ محاكيات على نفس
 * الكمبيوتر (أو أجهزة حقيقية على نفس شبكة الواي فاي) وتلعب لعبة كاملة.
 *
 * البروتوكول: كل رسالة سطر JSON واحد، نفس الترميز المستعمل مع Nearby.
 *
 * على المحاكي بتحتاج تربط المنافذ:
 * ```
 * adb -s emulator-5554 forward tcp:5599 tcp:5599   # جهاز المضيف
 * adb -s emulator-5556 reverse tcp:5599 tcp:5599   # كل جهاز لاعب
 * ```
 */
class LanConnectionsManager(
    private val hostAddress: String = DEFAULT_HOST,
    private val port: Int = DEFAULT_PORT
) : NearbyConnectionsManager {

    private val _events = MutableSharedFlow<ConnectionEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    override val events: SharedFlow<ConnectionEvent> = _events.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val clients = ConcurrentHashMap<String, BufferedWriter>()
    private val nextClientId = AtomicInteger(1)

    private var serverSocket: ServerSocket? = null
    private var hostSocket: Socket? = null
    private var hostWriter: BufferedWriter? = null
    private var job: Job? = null

    // ------------------------------------------------------------- المضيف

    override fun startAdvertising(serviceName: String) {
        if (job != null) return
        job = scope.launch {
            try {
                val server = ServerSocket(port)
                serverSocket = server
                Log.i(TAG, "listening on port $port")
                while (!server.isClosed) {
                    val socket = server.accept()
                    val endpointId = "lan-${nextClientId.getAndIncrement()}"
                    Log.i(TAG, "player connected: $endpointId")
                    clients[endpointId] = socket.writer()
                    emit(ConnectionEvent.EndpointConnected(endpointId))
                    readClient(socket, endpointId)
                }
            } catch (error: Exception) {
                if (serverSocket?.isClosed != true) {
                    Log.e(TAG, "advertising failed on port $port", error)
                    emit(ConnectionEvent.Error("تعذّر فتح الاستضافة: ${error.message}"))
                }
            }
        }
    }

    private fun readClient(socket: Socket, endpointId: String) {
        scope.launch {
            try {
                socket.reader().useLines { lines ->
                    lines.forEach { line ->
                        if (line.isNotBlank()) {
                            val message = decodeClientMessage(line.toByteArray())
                            emit(ConnectionEvent.ClientMessageReceived(endpointId, message))
                        }
                    }
                }
            } catch (_: Exception) {
                // انقطاع عادي — منبلّغ عنه تحت.
            } finally {
                clients.remove(endpointId)
                runCatching { socket.close() }
                emit(ConnectionEvent.EndpointDisconnected(endpointId))
            }
        }
    }

    // ------------------------------------------------------------- اللاعب

    override fun startDiscovery(serviceName: String) {
        if (job != null) return
        job = scope.launch {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(hostAddress, port), CONNECT_TIMEOUT_MS)
                hostSocket = socket
                hostWriter = socket.writer()
                emit(ConnectionEvent.EndpointConnected(HOST_ENDPOINT))

                socket.reader().useLines { lines ->
                    lines.forEach { line ->
                        if (line.isNotBlank()) {
                            emit(
                                ConnectionEvent.HostMessageReceived(
                                    decodeHostMessage(line.toByteArray())
                                )
                            )
                        }
                    }
                }
                emit(ConnectionEvent.EndpointDisconnected(HOST_ENDPOINT))
            } catch (error: Exception) {
                Log.e(TAG, "connect to $hostAddress:$port failed", error)
                emit(ConnectionEvent.Error("ما قدرنا نوصل للمضيف على $hostAddress:$port"))
            }
        }
    }

    // ------------------------------------------------------------- الإرسال

    override fun sendToEndpoint(endpointId: String, message: ClientMessage) {
        val writer = hostWriter ?: return
        write(writer) { encodeClientMessage(message).decodeToString() }
    }

    override fun sendToEndpoint(endpointId: String, message: HostMessage) {
        val writer = clients[endpointId] ?: return
        write(writer) { encodeHostMessage(message).decodeToString() }
    }

    override fun broadcastToAll(message: HostMessage) {
        val line = encodeHostMessage(message).decodeToString()
        clients.values.forEach { writer -> write(writer) { line } }
    }

    private fun write(writer: BufferedWriter, line: () -> String) {
        scope.launch {
            try {
                synchronized(writer) {
                    writer.write(line())
                    writer.newLine()
                    writer.flush()
                }
            } catch (error: Exception) {
                emit(ConnectionEvent.Error("تعذّر الإرسال: ${error.message}"))
            }
        }
    }

    override fun stop() {
        runCatching { serverSocket?.close() }
        runCatching { hostSocket?.close() }
        clients.values.forEach { writer -> runCatching { writer.close() } }
        clients.clear()
        job = null
        scope.cancel()
    }

    private fun emit(event: ConnectionEvent) {
        _events.tryEmit(event)
    }

    private fun Socket.reader(): BufferedReader = getInputStream().bufferedReader()

    private fun Socket.writer(): BufferedWriter = getOutputStream().bufferedWriter()

    companion object {
        /** ١٠.٠.٢.٢ هو جهاز الكمبيوتر بالنسبة للمحاكي. */
        const val DEFAULT_HOST = "127.0.0.1"
        const val DEFAULT_PORT = 5599
        const val HOST_ENDPOINT = "lan-host"
        private const val TAG = "FeudLan"
        private const val CONNECT_TIMEOUT_MS = 4_000
    }
}
