package com.feudparty.app.viewmodel

import com.feudparty.core.game.Answer
import com.feudparty.core.game.BuzzState
import com.feudparty.core.game.FastMoneyState
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

    private val fastMoneyQuestions = List(FastMoneyState.QUESTIONS_PER_PLAYER) { index ->
        Question("f$index", "سؤال سريع $index", listOf(Answer("جواب", 50)), "عام")
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        connections = FakeNearbyConnectionsManager()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        fastMoney: List<Question> = emptyList()
    ) = HostViewModel(
        connections = connections,
        questions = questions,
        fastMoneyQuestions = fastMoney,
        multipliers = listOf(1, 1)
    )

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
    fun `judging correct fills the round pot and broadcasts the new state`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connections.emit(ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Join("أ")))
        testScheduler.advanceUntilIdle()
        connections.emit(
            ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Buzz(TeamId.TEAM_1, 1L))
        )
        testScheduler.advanceUntilIdle()

        vm.judgeCorrect(0) // الجواب رقم ١ بياخد اللوح

        val state = vm.uiState.value
        assertEquals(RoundPhase.PLAY, state.phase)
        assertEquals(TeamId.TEAM_1, state.controllingTeam)
        assertEquals(60, state.pot)
        assertEquals(0, state.teams.getValue(TeamId.TEAM_1).score)

        val last = connections.broadcasts.last() as HostMessage.StateUpdate
        assertEquals(60, last.state.pot)
    }

    @Test
    fun `the state sent to teams hides the text of unrevealed answers`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connections.emit(ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Join("أ")))
        connections.emit(
            ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Buzz(TeamId.TEAM_1, 1L))
        )
        testScheduler.advanceUntilIdle()
        vm.judgeCorrect(0)

        val sent = (connections.broadcasts.last() as HostMessage.StateUpdate).state
        val answers = sent.currentQuestion!!.answers
        assertEquals("أ", answers[0].text)
        assertEquals("", answers[1].text)
        // نسخة المضيف بتضل كاملة.
        assertEquals("ب", vm.uiState.value.currentQuestion!!.answers[1].text)
    }

    @Test
    fun `three strikes on the host open the steal for the other team`() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        connections.emit(ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Join("أ")))
        connections.emit(ConnectionEvent.ClientMessageReceived("ep-b", ClientMessage.Join("ب")))
        connections.emit(
            ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Buzz(TeamId.TEAM_1, 1L))
        )
        testScheduler.advanceUntilIdle()
        vm.judgeCorrect(0)
        repeat(3) { vm.judgeWrong() }

        assertEquals(RoundPhase.STEAL, vm.uiState.value.phase)
        assertEquals(TeamId.TEAM_2, vm.uiState.value.stealingTeam)
    }

    @Test
    fun `the fast money clock ticks once a second and stops with the round`() = runTest(dispatcher) {
        val vm = viewModel(fastMoney = fastMoneyQuestions)
        testScheduler.advanceUntilIdle()
        connections.emit(ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Join("أ")))
        connections.emit(
            ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Buzz(TeamId.TEAM_1, 1L))
        )
        testScheduler.advanceUntilIdle()

        vm.judgeCorrect(0)
        vm.judgeCorrect(1) // انكشف اللوح كله فانتهت الجولة الأولى
        vm.nextRound()
        connections.emit(
            ConnectionEvent.ClientMessageReceived("ep-a", ClientMessage.Buzz(TeamId.TEAM_1, 2L))
        )
        testScheduler.advanceUntilIdle()
        vm.judgeCorrect(0)
        vm.nextRound()

        assertEquals(RoundPhase.FAST_MONEY, vm.uiState.value.phase)

        vm.startFastMoneyTimer()
        testScheduler.advanceTimeBy(3_100)
        assertEquals(
            FastMoneyState.FIRST_PLAYER_SECONDS - 3,
            vm.uiState.value.fastMoney!!.secondsRemaining
        )

        vm.endGame()
        testScheduler.advanceTimeBy(5_000)
        assertTrue(vm.uiState.value.gameOver)
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
