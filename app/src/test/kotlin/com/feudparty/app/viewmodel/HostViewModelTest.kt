package com.feudparty.app.viewmodel

import com.feudparty.core.game.Answer
import com.feudparty.core.game.BuzzState
import com.feudparty.core.game.Question
import com.feudparty.core.game.RoundPhase
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
import org.junit.Assert.assertFalse
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

    private fun viewModel() = HostViewModel(
        connections = connections,
        questions = questions,
        multipliers = listOf(1, 1)
    )

    private fun join(vararg endpoints: String) {
        endpoints.forEach { endpointId ->
            connections.emit(
                ConnectionEvent.ClientMessageReceived(endpointId, ClientMessage.Join(endpointId))
            )
        }
    }

    /** القرار بيجي من جهاز اللاعب اللي كسب المواجهة. */
    private fun choose(endpointId: String, play: Boolean) {
        connections.emit(
            ConnectionEvent.ClientMessageReceived(
                endpointId,
                ClientMessage.Choose(endpointId, play)
            )
        )
    }

    private fun buzz(endpointId: String, atMillis: Long = 1L) {
        connections.emit(
            ConnectionEvent.ClientMessageReceived(
                endpointId,
                ClientMessage.Buzz(endpointId, atMillis)
            )
        )
    }

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
    fun `players are handed out to keep the teams balanced`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        join("ep-a", "ep-b", "ep-c", "ep-d")
        testScheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(listOf("ep-a", "ep-c"), state.playersOf(TeamId.TEAM_1).map { it.id })
        assertEquals(listOf("ep-b", "ep-d"), state.playersOf(TeamId.TEAM_2).map { it.id })
        assertEquals(
            listOf(
                "ep-a" to HostMessage.Assigned("ep-a", TeamId.TEAM_1),
                "ep-b" to HostMessage.Assigned("ep-b", TeamId.TEAM_2),
                "ep-c" to HostMessage.Assigned("ep-c", TeamId.TEAM_1),
                "ep-d" to HostMessage.Assigned("ep-d", TeamId.TEAM_2)
            ),
            connections.directHostMessages
        )
    }

    @Test
    fun `only the podium player of a team can buzz`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        join("ep-a", "ep-b", "ep-c")
        testScheduler.advanceUntilIdle()

        buzz("ep-c") // نفس فريق ep-a بس مش لاعب المنصة
        testScheduler.advanceUntilIdle()
        assertEquals(BuzzState.OPEN, vm.uiState.value.buzzState)

        buzz("ep-b")
        testScheduler.advanceUntilIdle()
        assertEquals(BuzzState.LOCKED_TEAM_2, vm.uiState.value.buzzState)
        assertEquals("ep-b", vm.uiState.value.buzzedPlayerId)
    }

    @Test
    fun `buzz from an unknown endpoint is ignored`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        join("ep-a", "ep-b")
        buzz("stranger")
        testScheduler.advanceUntilIdle()

        assertEquals(BuzzState.OPEN, vm.uiState.value.buzzState)
        assertNull(vm.uiState.value.buzzedPlayerId)
    }

    @Test
    fun `judging correct fills the round pot and passes the turn down the line`() =
        runTest(dispatcher) {
            val vm = viewModel()
            testScheduler.advanceUntilIdle()
            join("ep-a", "ep-b", "ep-c")
            buzz("ep-a")
            testScheduler.advanceUntilIdle()

            vm.judgeCorrect(0) // الجواب رقم ١ بيكسب المواجهة
            choose("ep-a", play = true)
            testScheduler.advanceUntilIdle()

            val state = vm.uiState.value
            assertEquals(RoundPhase.PLAY, state.phase)
            assertEquals(TeamId.TEAM_1, state.controllingTeam)
            assertEquals(60, state.pot)
            assertEquals(0, state.teams.getValue(TeamId.TEAM_1).score)
            assertEquals("ep-c", state.turnPlayerId) // مش نفس اللاعب اللي جاوب
        }

    @Test
    fun `players never receive the hidden answers`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        join("ep-a", "ep-b")
        buzz("ep-a")
        testScheduler.advanceUntilIdle()
        vm.judgeCorrect(0)

        val sent = (connections.broadcasts.last() as HostMessage.StateUpdate).state
        val question = sent.currentQuestion!!
        // المكشوف بس بيوصل؛ الباقي خانات فاضية.
        assertEquals("أ", question.answers[0].text)
        assertEquals("", question.answers[1].text)
        // نسخة المضيف بتضل كاملة.
        assertEquals("سؤال١", vm.uiState.value.currentQuestion!!.text)
    }

    @Test
    fun `the question reaches players only after somebody buzzes`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        join("ep-a", "ep-b")
        testScheduler.advanceUntilIdle()

        val beforeBuzz = (connections.broadcasts.last() as HostMessage.StateUpdate).state
        assertEquals("", beforeBuzz.currentQuestion!!.text)

        buzz("ep-a")
        testScheduler.advanceUntilIdle()

        val afterBuzz = (connections.broadcasts.last() as HostMessage.StateUpdate).state
        assertEquals("سؤال١", afterBuzz.currentQuestion!!.text)
    }

    @Test
    fun `three strikes hand the steal to the other team's podium player`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        join("ep-a", "ep-b")
        buzz("ep-a")
        testScheduler.advanceUntilIdle()
        vm.judgeCorrect(0)
        choose("ep-a", play = true)
        testScheduler.advanceUntilIdle()
        repeat(3) { vm.judgeWrong() }

        val state = vm.uiState.value
        assertEquals(RoundPhase.STEAL, state.phase)
        assertEquals(TeamId.TEAM_2, state.stealingTeam)
        assertEquals("ep-b", state.turnPlayerId)
    }

    @Test
    fun `a disconnected device is marked offline and keeps its place`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        join("ep-a", "ep-b")
        testScheduler.advanceUntilIdle()
        connections.emit(ConnectionEvent.EndpointDisconnected("ep-a"))
        testScheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.player("ep-a")!!.connected)
        assertFalse(vm.uiState.value.teams.getValue(TeamId.TEAM_1).connected)

        join("ep-a")
        testScheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value.player("ep-a")!!.connected)
        assertEquals(1, vm.uiState.value.playersOf(TeamId.TEAM_1).size)
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


    @Test
    fun `only the winning podium player's choice is accepted`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        join("ep-a", "ep-b")
        buzz("ep-a")
        testScheduler.advanceUntilIdle()
        vm.judgeCorrect(0)

        // ep-b مش صاحب القرار — بينتجاهل.
        connections.emit(
            ConnectionEvent.ClientMessageReceived("ep-b", ClientMessage.Choose("ep-b", false))
        )
        testScheduler.advanceUntilIdle()
        assertEquals(RoundPhase.PLAY_OR_PASS, vm.uiState.value.phase)

        connections.emit(
            ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Choose("ep-a", false))
        )
        testScheduler.advanceUntilIdle()

        // مرّرها، فاللوح راح للفريق التاني.
        assertEquals(RoundPhase.PLAY, vm.uiState.value.phase)
        assertEquals(TeamId.TEAM_2, vm.uiState.value.controllingTeam)
    }

    @Test
    fun `next round shows everyone the scoreboard first`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        join("ep-a", "ep-b")
        buzz("ep-a")
        testScheduler.advanceUntilIdle()
        vm.judgeCorrect(0)
        choose("ep-a", play = true)
        testScheduler.advanceUntilIdle()
        vm.judgeCorrect(1) // انكشف اللوح كله
        vm.nextRound()

        val sent = (connections.broadcasts.last() as HostMessage.StateUpdate).state
        assertEquals(RoundPhase.SCOREBOARD, sent.phase)
        assertEquals(RoundPhase.SCOREBOARD, vm.uiState.value.phase)
    }
}
