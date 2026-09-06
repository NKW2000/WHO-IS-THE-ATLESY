package com.feudparty.app.viewmodel

import com.feudparty.core.game.Answer
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Player
import com.feudparty.core.game.PlayerMark
import com.feudparty.core.game.Question
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState
import com.feudparty.core.network.ClientMessage
import com.feudparty.core.network.ConnectionEvent
import com.feudparty.core.network.HostMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var connections: FakeNearbyConnectionsManager

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        connections = FakeNearbyConnectionsManager()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = PlayerViewModel(connections, clock = { 777L })

    private fun state() = GameState(
        questions = listOf(Question("q1", "", listOf(Answer("", 100)), "عام")),
        players = listOf(
            Player("p-a", "سامر", TeamId.TEAM_1, seat = 1),
            Player("p-b", "ليلى", TeamId.TEAM_2, seat = 1)
        ),
        teams = mapOf(
            TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "الفريق الأحمر"),
            TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "الفريق الأزرق")
        )
    )

    private fun connect(vm: PlayerViewModel, playerId: String = "p-b") {
        vm.join("ليلى")
        connections.emit(ConnectionEvent.EndpointConnected("host-1"))
        connections.emit(
            ConnectionEvent.HostMessageReceived(HostMessage.Assigned(playerId, TeamId.TEAM_2))
        )
    }

    @Test
    fun `join starts discovery and holds the name until the host is found`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.join("سامر")
        testScheduler.advanceUntilIdle()

        assertEquals(PlayerViewModel.ConnectionStatus.SEARCHING, vm.status.value)
        assertTrue("ما لازم نبعت اسم قبل ما نتصل", connections.clientMessages.isEmpty())

        connections.emit(ConnectionEvent.EndpointConnected("host-1"))
        testScheduler.advanceUntilIdle()

        assertEquals(PlayerViewModel.ConnectionStatus.CONNECTED, vm.status.value)
        assertEquals(listOf("host-1" to ClientMessage.Join("سامر")), connections.clientMessages)
    }

    @Test
    fun `the host decides this device's player id and team`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connect(vm)
        testScheduler.advanceUntilIdle()

        assertEquals("p-b", vm.playerId.value)
        assertEquals(TeamId.TEAM_2, vm.teamId.value)
    }

    @Test
    fun `the buzzer only works when the host armed this player`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connect(vm)
        connections.emit(ConnectionEvent.HostMessageReceived(HostMessage.StateUpdate(state())))
        testScheduler.advanceUntilIdle()

        assertTrue(vm.canBuzz()) // p-b هو لاعب المنصة لفريقه
        assertEquals(PlayerMark.ARMED, vm.mark())
        vm.onBuzzTapped()
        assertEquals(
            "host-1" to ClientMessage.Buzz("p-b", 777L),
            connections.clientMessages.last()
        )
    }

    @Test
    fun `a player who is not on turn cannot buzz`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connect(vm)
        val playing = state().copy(
            phase = RoundPhase.PLAY,
            controllingTeam = TeamId.TEAM_1,
            turnPlayerId = "p-a"
        )
        connections.emit(ConnectionEvent.HostMessageReceived(HostMessage.StateUpdate(playing)))
        testScheduler.advanceUntilIdle()

        assertFalse(vm.canBuzz())
        assertEquals(PlayerMark.IDLE, vm.mark())
        vm.onBuzzTapped()
        assertTrue(connections.clientMessages.none { it.second is ClientMessage.Buzz })
    }

    @Test
    fun `a wrong ruling turns this device red`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connect(vm)
        val scolded = state().copy(
            phase = RoundPhase.PLAY,
            controllingTeam = TeamId.TEAM_2,
            turnPlayerId = "p-a",
            wrongPlayers = setOf("p-b")
        )
        connections.emit(ConnectionEvent.HostMessageReceived(HostMessage.StateUpdate(scolded)))
        testScheduler.advanceUntilIdle()

        assertEquals(PlayerMark.WRONG, vm.mark())
        assertFalse(vm.canBuzz())
    }

    @Test
    fun `buzzing does nothing before the host assigns a player id`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.join("ليلى")
        connections.emit(ConnectionEvent.EndpointConnected("host-1"))
        connections.emit(ConnectionEvent.HostMessageReceived(HostMessage.StateUpdate(state())))
        testScheduler.advanceUntilIdle()

        assertNull(vm.playerId.value)
        vm.onBuzzTapped()
        assertTrue(connections.clientMessages.none { it.second is ClientMessage.Buzz })
    }

    @Test
    fun `losing the host flips the status to disconnected`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connect(vm)
        testScheduler.advanceUntilIdle()
        connections.emit(ConnectionEvent.EndpointDisconnected("host-1"))
        testScheduler.advanceUntilIdle()

        assertEquals(PlayerViewModel.ConnectionStatus.DISCONNECTED, vm.status.value)
        assertFalse(vm.canBuzz())
    }
}
