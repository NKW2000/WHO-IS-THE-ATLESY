package com.feudparty.core.network

import com.feudparty.core.game.Answer
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.ServerSocket

/**
 * وصل حقيقي عبر سوكِت على نفس الجهاز — هيك منتأكد إنه وضع الاختبار
 * بيوصّل الرسائل بالاتجاهين قبل ما نجرّبه على المحاكيات.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LanConnectionsManagerTest {

    private val port = freePort()
    private val host = LanConnectionsManager(port = port)
    private val player = LanConnectionsManager(hostAddress = "127.0.0.1", port = port)

    private fun freePort(): Int = ServerSocket(0).use { it.localPort }

    @After
    fun tearDown() {
        host.stop()
        player.stop()
    }

    private fun state() = GameState(
        questions = listOf(Question("q1", "سؤال", listOf(Answer("أ", 40)), "عام")),
        teams = mapOf(
            TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "الأحمر"),
            TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الأزرق")
        )
    )

    @Test
    fun `a player connects to the host and both directions carry messages`() = runBlocking {
        val hostEvents = mutableListOf<ConnectionEvent>()
        val playerEvents = mutableListOf<ConnectionEvent>()

        val hostCollector = launch(start = CoroutineStart.UNDISPATCHED) {
            host.events.collect { hostEvents += it }
        }
        val playerCollector = launch(start = CoroutineStart.UNDISPATCHED) {
            player.events.collect { playerEvents += it }
        }

        host.startAdvertising("feud-party")
        player.startDiscovery("feud-party")

        // اللاعب ← المضيف
        withTimeout(5_000) {
            while (hostEvents.none { it is ConnectionEvent.EndpointConnected }) {
                kotlinx.coroutines.delay(20)
            }
        }
        val endpointId = (hostEvents.first { it is ConnectionEvent.EndpointConnected }
            as ConnectionEvent.EndpointConnected).endpointId

        player.sendToEndpoint(LanConnectionsManager.HOST_ENDPOINT, ClientMessage.Join("سامر"))
        withTimeout(5_000) {
            while (hostEvents.none { it is ConnectionEvent.ClientMessageReceived }) {
                kotlinx.coroutines.delay(20)
            }
        }
        val received = hostEvents.filterIsInstance<ConnectionEvent.ClientMessageReceived>().first()
        assertEquals(endpointId, received.endpointId)
        assertEquals(ClientMessage.Join("سامر"), received.message)

        // المضيف ← اللاعب
        host.broadcastToAll(HostMessage.StateUpdate(state()))
        withTimeout(5_000) {
            while (playerEvents.none { it is ConnectionEvent.HostMessageReceived }) {
                kotlinx.coroutines.delay(20)
            }
        }
        val update = playerEvents.filterIsInstance<ConnectionEvent.HostMessageReceived>().first()
        assertTrue(update.message is HostMessage.StateUpdate)

        hostCollector.cancel()
        playerCollector.cancel()
    }

    @Test
    fun `a player that cannot reach the host reports it instead of hanging`() = runBlocking {
        val lonely = LanConnectionsManager(hostAddress = "127.0.0.1", port = freePort())
        val events = mutableListOf<ConnectionEvent>()
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            lonely.events.collect { events += it }
        }
        try {
            lonely.startDiscovery("feud-party")
            withTimeout(10_000) {
                while (events.none { it is ConnectionEvent.Error }) {
                    kotlinx.coroutines.delay(20)
                }
            }
            val error = events.filterIsInstance<ConnectionEvent.Error>().first()
            assertTrue(error.description, error.description.contains("127.0.0.1"))
        } finally {
            collector.cancel()
            lonely.stop()
        }
    }
}
