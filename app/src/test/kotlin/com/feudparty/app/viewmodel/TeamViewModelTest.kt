package com.feudparty.app.viewmodel

import com.feudparty.core.game.Answer
import com.feudparty.core.game.BuzzState
import com.feudparty.core.game.GameState
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
class TeamViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var connections: FakeNearbyConnectionsManager

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        connections = FakeNearbyConnectionsManager()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = TeamViewModel(connections, clock = { 777L })

    private fun stateWith(buzzState: BuzzState) = GameState(
        questions = listOf(Question("q1", "سؤال", listOf(Answer("أ", 100)), "عام")),
        teams = mapOf(
            TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "فريق ١"),
            TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "فريق ٢")
        ),
        buzzState = buzzState
    )

    @Test
    fun `join starts discovery and holds the name until the host is found`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.join("النجوم")
        testScheduler.advanceUntilIdle()

        assertEquals(TeamViewModel.ConnectionStatus.SEARCHING, vm.status.value)
        assertTrue("ما لازم نبعت اسم قبل ما نتصل", connections.clientMessages.isEmpty())

        connections.emit(ConnectionEvent.EndpointConnected("host-1"))
        testScheduler.advanceUntilIdle()

        assertEquals(TeamViewModel.ConnectionStatus.CONNECTED, vm.status.value)
        assertEquals(listOf("host-1" to ClientMessage.Join("النجوم")), connections.clientMessages)
    }

    @Test
    fun `assigned message decides which team this device buzzes for`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.join("الصقور")
        connections.emit(ConnectionEvent.EndpointConnected("host-1"))
        connections.emit(ConnectionEvent.HostMessageReceived(HostMessage.Assigned(TeamId.TEAM_2)))
        connections.emit(ConnectionEvent.HostMessageReceived(HostMessage.StateUpdate(stateWith(BuzzState.OPEN))))
        testScheduler.advanceUntilIdle()

        assertEquals(TeamId.TEAM_2, vm.assignedTeam.value)
        vm.onBuzzTapped()

        assertEquals(
            "host-1" to ClientMessage.Buzz(TeamId.TEAM_2, 777L),
            connections.clientMessages.last()
        )
    }

    @Test
    fun `buzzing does nothing before the host assigns a team`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.join("الصقور")
        connections.emit(ConnectionEvent.EndpointConnected("host-1"))
        connections.emit(ConnectionEvent.HostMessageReceived(HostMessage.StateUpdate(stateWith(BuzzState.OPEN))))
        testScheduler.advanceUntilIdle()

        assertNull(vm.assignedTeam.value)
        vm.onBuzzTapped()

        assertTrue(connections.clientMessages.none { it.second is ClientMessage.Buzz })
        assertFalse(vm.canBuzz())
    }

    @Test
    fun `buzzing is refused once another team has locked the buzzer`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.join("الصقور")
        connections.emit(ConnectionEvent.EndpointConnected("host-1"))
        connections.emit(ConnectionEvent.HostMessageReceived(HostMessage.Assigned(TeamId.TEAM_2)))
        connections.emit(
            ConnectionEvent.HostMessageReceived(HostMessage.StateUpdate(stateWith(BuzzState.LOCKED_TEAM_1)))
        )
        testScheduler.advanceUntilIdle()

        assertFalse(vm.canBuzz())
        vm.onBuzzTapped()
        assertTrue(connections.clientMessages.none { it.second is ClientMessage.Buzz })
    }

    @Test
    fun `state updates from the host are mirrored as-is`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        val hostState = stateWith(BuzzState.LOCKED_TEAM_1)
        connections.emit(ConnectionEvent.HostMessageReceived(HostMessage.StateUpdate(hostState)))
        testScheduler.advanceUntilIdle()
        assertEquals(hostState, vm.gameState.value)
    }

    @Test
    fun `losing the host flips the status to disconnected`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.join("الصقور")
        connections.emit(ConnectionEvent.EndpointConnected("host-1"))
        testScheduler.advanceUntilIdle()
        connections.emit(ConnectionEvent.EndpointDisconnected("host-1"))
        testScheduler.advanceUntilIdle()

        assertEquals(TeamViewModel.ConnectionStatus.DISCONNECTED, vm.status.value)
        assertFalse(vm.canBuzz())
    }

    @Test
    fun `buzzing is refused outside the face-off phase`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.join("الصقور")
        connections.emit(ConnectionEvent.EndpointConnected("host-1"))
        connections.emit(ConnectionEvent.HostMessageReceived(HostMessage.Assigned(TeamId.TEAM_2)))
        connections.emit(
            ConnectionEvent.HostMessageReceived(
                HostMessage.StateUpdate(
                    stateWith(BuzzState.OPEN).copy(
                        phase = RoundPhase.PLAY,
                        controllingTeam = TeamId.TEAM_2
                    )
                )
            )
        )
        testScheduler.advanceUntilIdle()

        assertFalse(vm.canBuzz())
        vm.onBuzzTapped()
        assertTrue(connections.clientMessages.none { it.second is ClientMessage.Buzz })
    }
}
