package com.feudparty.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineFaceOffTest {

    @Test
    fun `first buzz locks the other team out`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_2)
        val result = engine.buzzPodium(TeamId.TEAM_1)

        assertEquals(BuzzState.LOCKED_TEAM_2, result.buzzState)
        assertEquals(TeamId.TEAM_2, result.faceOffTeam)
    }

    @Test
    fun `top answer in face-off wins control immediately`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        val result = engine.correct(0)

        assertEquals(RoundPhase.PLAY_OR_PASS, result.phase)
        assertEquals(TeamId.TEAM_1, result.faceOffWinner)
        assertEquals(40, result.pot)
        assertEquals(0, result.score(TeamId.TEAM_1)) // النقاط بتنحسب بنهاية الجولة
    }

    @Test
    fun `lower answer gives the other team a chance to beat it`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        val result = engine.correct(2) // ٢٠ نقطة

        assertEquals(RoundPhase.FACE_OFF_SECOND, result.phase)
        assertEquals(TeamId.TEAM_2, result.faceOffTeam)
        assertEquals(TeamId.TEAM_1, result.faceOffLeader)
        assertEquals(20, result.faceOffLeaderPoints)
        assertEquals(BuzzState.CLOSED, result.buzzState)
    }

    @Test
    fun `higher second answer steals control`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        engine.correct(2) // فريق ١ = ٢٠
        val result = engine.correct(1) // فريق ٢ = ٣٠

        assertEquals(RoundPhase.PLAY_OR_PASS, result.phase)
        assertEquals(TeamId.TEAM_2, result.faceOffWinner)
        assertEquals(50, result.pot)
    }

    @Test
    fun `lower second answer leaves control with the leader`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        engine.correct(1) // فريق ١ = ٣٠
        val result = engine.correct(3) // فريق ٢ = ١٠

        assertEquals(TeamId.TEAM_1, result.faceOffWinner)
        assertEquals(40, result.pot)
    }

    @Test
    fun `wrong first answer passes the face-off to the other team`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        val result = engine.wrong()

        assertEquals(RoundPhase.FACE_OFF_SECOND, result.phase)
        assertEquals(TeamId.TEAM_2, result.faceOffTeam)
        assertNull(result.faceOffLeader)
    }

    @Test
    fun `both wrong reopens the buzzer on the same question`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        engine.wrong()
        val result = engine.wrong()

        assertEquals(RoundPhase.FACE_OFF, result.phase)
        assertEquals(BuzzState.OPEN, result.buzzState)
        assertEquals(0, result.currentQuestionIndex)
        assertNull(result.faceOffTeam)
    }

    @Test
    fun `wrong first answer hands the other team a full clock`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        engine.apply(GameEvent.Tick)
        engine.apply(GameEvent.Tick)
        val result = engine.wrong()

        assertEquals(result.answerLimitSeconds, result.answerSecondsLeft)
    }

    @Test
    fun `every wrong answer bumps the counter that drives the sound`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        val first = engine.wrong()
        val second = engine.wrong()

        assertEquals(1, first.wrongTicks)
        assertEquals(2, second.wrongTicks)
    }

    @Test
    fun `both wrong moves the podium on and asks the host for a new question`() {
        val engine = GameEngine(freshState())
        val seat = engine.state.faceOffSeat
        engine.buzzPodium(TeamId.TEAM_1)
        engine.wrong()
        val result = engine.wrong()

        assertEquals(seat + 1, result.faceOffSeat)
        assertTrue(result.faceOffFailed)
    }

    @Test
    fun `a replaced question clears the failed face-off flag`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        engine.wrong()
        engine.wrong()
        val result = engine.apply(GameEvent.ReplaceQuestion(board("q9")))

        assertEquals(false, result.faceOffFailed)
        assertEquals(RoundPhase.FACE_OFF, result.phase)
    }

    @Test
    fun `second team correct after first was wrong takes control`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        engine.wrong()
        val result = engine.correct(3)

        assertEquals(RoundPhase.PLAY_OR_PASS, result.phase)
        assertEquals(TeamId.TEAM_2, result.faceOffWinner)
    }

    @Test
    fun `buzz is ignored once the board is being played`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        val result = engine.buzzPodium(TeamId.TEAM_2)

        assertEquals(BuzzState.CLOSED, result.buzzState)
        assertEquals(TeamId.TEAM_1, result.controllingTeam)
    }

    @Test
    fun `revealed answer cannot be judged twice`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        val result = engine.correct(0)

        assertEquals(40, result.pot)
        assertTrue(result.currentQuestion!!.answers[0].revealed)
    }

    @Test
    fun `the face-off winner is asked to play or pass, and only that player is armed`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        val choice = engine.correct(0)

        assertEquals(RoundPhase.PLAY_OR_PASS, choice.phase)
        assertEquals(TeamId.TEAM_1, choice.faceOffWinner)
        // بس لاعب المنصة اللي كسب بيقرر.
        assertEquals(setOf("a1"), choice.armedPlayerIds())
        assertNull(choice.controllingTeam)
    }

    @Test
    fun `choosing to play keeps the board with the winner`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        engine.correct(0)
        val result = engine.choosePlay()

        assertEquals(RoundPhase.PLAY, result.phase)
        assertEquals(TeamId.TEAM_1, result.controllingTeam)
        assertEquals("a2", result.turnPlayerId)
    }

    @Test
    fun `passing hands the board to the other team`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        engine.correct(0)
        val result = engine.choosePass()

        assertEquals(RoundPhase.PLAY, result.phase)
        assertEquals(TeamId.TEAM_2, result.controllingTeam)
        // الفريق التاني كمان بيبلّش من اللاعب اللي بعد لاعب منصته.
        assertEquals("b2", result.turnPlayerId)
    }

    @Test
    fun `a choice outside the choosing phase is ignored`() {
        val engine = GameEngine(freshState())
        val result = engine.choosePlay()

        assertEquals(RoundPhase.FACE_OFF, result.phase)
        assertNull(result.controllingTeam)
    }
}
