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

        assertEquals(RoundPhase.PLAY, result.phase)
        assertEquals(TeamId.TEAM_1, result.controllingTeam)
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

        assertEquals(RoundPhase.PLAY, result.phase)
        assertEquals(TeamId.TEAM_2, result.controllingTeam)
        assertEquals(50, result.pot)
    }

    @Test
    fun `lower second answer leaves control with the leader`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        engine.correct(1) // فريق ١ = ٣٠
        val result = engine.correct(3) // فريق ٢ = ١٠

        assertEquals(TeamId.TEAM_1, result.controllingTeam)
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
    fun `second team correct after first was wrong takes control`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        engine.wrong()
        val result = engine.correct(3)

        assertEquals(RoundPhase.PLAY, result.phase)
        assertEquals(TeamId.TEAM_2, result.controllingTeam)
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
}
