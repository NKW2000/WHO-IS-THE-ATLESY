package com.feudparty.app.viewmodel

import com.feudparty.core.game.Answer
import com.feudparty.core.game.BuzzState
import com.feudparty.core.game.Question
import com.feudparty.core.game.TeamId
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HostViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var connections: FakeNearbyConnectionsManager

    private val questions = listOf(
        Question("q1", "سؤال١", listOf(Answer("أ", 60), Answer("ب", 40)), "عام"),
        Question("q2", "سؤال٢", listOf(Answer("ج", 100)), "عام")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        connections = FakeNearbyConnectionsManager()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = HostViewModel(connections, questions)

    @Test
    fun `startHosting advertises once under the shared service name`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.startHosting()
        vm.startHosting()
        assertEquals(HostViewModel.SERVICE_NAME, connections.advertisingAs)
        assertTrue(vm.advertising.value)
    }

    @Test
    fun `first team to join is assigned team one and the second team two`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connections.emit(ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Join("النجوم")))
        connections.emit(ConnectionEvent.ClientMessageReceived("ep-b", ClientMessage.Join("الصقور")))
        testScheduler.advanceUntilIdle()

        assertEquals(
            listOf(
                "ep-a" to HostMessage.Assigned(TeamId.TEAM_1),
                "ep-b" to HostMessage.Assigned(TeamId.TEAM_2)
            ),
            connections.directHostMessages
        )
        val teams = vm.uiState.value.teams
        assertEquals("النجوم", teams.getValue(TeamId.TEAM_1).name)
        assertEquals("الصقور", teams.getValue(TeamId.TEAM_2).name)
        assertTrue(teams.values.all { it.connected })
    }

    @Test
    fun `a third device is refused instead of stealing a team`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connections.emit(ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Join("أ")))
        connections.emit(ConnectionEvent.ClientMessageReceived("ep-b", ClientMessage.Join("ب")))
        connections.emit(ConnectionEvent.ClientMessageReceived("ep-c", ClientMessage.Join("ج")))
        testScheduler.advanceUntilIdle()

        assertEquals(2, connections.directHostMessages.size)
        assertEquals("اللعبة ممتلئة — بس فريقين بيقدروا ينضموا", vm.lastError.value)
    }

    @Test
    fun `buzz is attributed to the endpoint's assigned team not to the claimed one`() =
        runTest(dispatcher) {
            val vm = viewModel()
            testScheduler.advanceUntilIdle()
        testScheduler.advanceUntilIdle()
            connections.emit(ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Join("أ")))
            connections.emit(ConnectionEvent.ClientMessageReceived("ep-b", ClientMessage.Join("ب")))
            testScheduler.advanceUntilIdle()

            // جهاز الفريق الثاني بيدّعي إنو فريق ١ — المضيف لازم يتجاهل الادعاء.
            connections.emit(
                ConnectionEvent.ClientMessageReceived("ep-b", ClientMessage.Buzz(TeamId.TEAM_1, 5L))
            )
            testScheduler.advanceUntilIdle()

            assertEquals(BuzzState.LOCKED_TEAM_2, vm.uiState.value.buzzState)
        }

    @Test
    fun `buzz from an unknown endpoint is ignored`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connections.emit(
            ConnectionEvent.ClientMessageReceived("stranger", ClientMessage.Buzz(TeamId.TEAM_1, 1L))
        )
        testScheduler.advanceUntilIdle()
        assertEquals(BuzzState.OPEN, vm.uiState.value.buzzState)
    }

    @Test
    fun `judging correct awards points and broadcasts the new state`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connections.emit(ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Join("أ")))
        testScheduler.advanceUntilIdle()
        connections.emit(
            ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Buzz(TeamId.TEAM_1, 1L))
        )
        testScheduler.advanceUntilIdle()

        vm.judgeCorrect(0)

        assertEquals(60, vm.uiState.value.teams.getValue(TeamId.TEAM_1).score)
        val last = connections.broadcasts.last() as HostMessage.StateUpdate
        assertEquals(60, last.state.teams.getValue(TeamId.TEAM_1).score)
    }

    @Test
    fun `disconnecting a team marks it offline and frees its slot`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connections.emit(ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Join("أ")))
        testScheduler.advanceUntilIdle()
        connections.emit(ConnectionEvent.EndpointDisconnected("ep-a"))
        testScheduler.advanceUntilIdle()

        assertEquals(false, vm.uiState.value.teams.getValue(TeamId.TEAM_1).connected)

        // نفس الجهاز يرجع يتصل → يرجع ياخد نفس الفريق (الخانة صارت فاضية).
        connections.emit(ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Join("أ")))
        testScheduler.advanceUntilIdle()
        assertEquals(
            HostMessage.Assigned(TeamId.TEAM_1),
            connections.directHostMessages.last().second
        )
        assertTrue(vm.uiState.value.teams.getValue(TeamId.TEAM_1).connected)
    }

    @Test
    fun `connection errors surface to the host and can be dismissed`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connections.emit(ConnectionEvent.Error("تعذّر بدء البث"))
        testScheduler.advanceUntilIdle()
        assertEquals("تعذّر بدء البث", vm.lastError.value)
        vm.dismissError()
        assertNull(vm.lastError.value)
    }
}
