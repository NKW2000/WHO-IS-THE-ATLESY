package com.feudparty.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineRoundTest {

    @Test
    fun `clearing the board awards the whole pot to the controlling team`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        engine.correct(1)
        engine.correct(2)
        val result = engine.correct(3)

        assertEquals(RoundPhase.ROUND_END, result.phase)
        assertEquals(TeamId.TEAM_1, result.roundWinner)
        assertEquals(100, result.score(TeamId.TEAM_1))
        assertEquals(0, result.score(TeamId.TEAM_2))
        assertEquals(Award(TeamId.TEAM_1, 100, stolen = false), result.lastAward)
    }

    @Test
    fun `three strikes open the steal for the other team`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        engine.wrong()
        engine.wrong()
        val two = engine.state
        assertEquals(RoundPhase.PLAY, two.phase)
        assertEquals(2, two.strikes)

        val result = engine.wrong()
        assertEquals(RoundPhase.STEAL, result.phase)
        assertEquals(3, result.strikes)
        assertEquals(TeamId.TEAM_2, result.stealingTeam)
    }

    @Test
    fun `successful steal gives the pot to the stealing team`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1) // ٤٠ بالـ pot
        repeat(3) { engine.wrong() }
        val result = engine.correct(1) // +٣٠

        assertEquals(70, result.score(TeamId.TEAM_2))
        assertEquals(0, result.score(TeamId.TEAM_1))
        assertEquals(Award(TeamId.TEAM_2, 70, stolen = true), result.lastAward)
    }

    @Test
    fun `failed steal returns the pot to the controlling team`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        repeat(3) { engine.wrong() }
        val result = engine.wrong()

        assertEquals(40, result.score(TeamId.TEAM_1))
        assertEquals(0, result.score(TeamId.TEAM_2))
        assertEquals(RoundPhase.ROUND_END, result.phase)
    }

    @Test
    fun `round end reveals every answer on the board`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        repeat(3) { engine.wrong() }
        val result = engine.wrong()

        assertTrue(result.currentQuestion!!.answers.all { it.revealed })
    }

    @Test
    fun `round multiplier scales the pot`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        repeat(3) { engine.wrong() }
        engine.wrong() // نهاية الجولة الأولى
        engine.apply(GameEvent.NextRound) // شاشة النتائج
        engine.apply(GameEvent.NextRound) // الجولة الجاية

        engine.giveControlTo(TeamId.TEAM_2)
        repeat(3) { engine.wrong() }
        val result = engine.wrong()

        // الجولة التانية × ٢ — ٤٠ بالـ pot بتصير ٨٠
        assertEquals(2, result.multiplier)
        assertEquals(80, result.score(TeamId.TEAM_2))
    }

    @Test
    fun `next round resets the board state`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        engine.wrong()
        repeat(3) { engine.wrong() }
        engine.apply(GameEvent.NextRound)
        val result = engine.apply(GameEvent.NextRound)

        assertEquals(1, result.currentQuestionIndex)
        assertEquals(RoundPhase.FACE_OFF, result.phase)
        assertEquals(BuzzState.OPEN, result.buzzState)
        assertEquals(0, result.pot)
        assertEquals(0, result.strikes)
        assertNull(result.controllingTeam)
        assertNull(result.roundWinner)
        assertFalse(result.gameOver)
    }

    @Test
    fun `next round after the last question ends the game`() {
        val engine = GameEngine(freshState())
        engine.apply(GameEvent.NextRound)
        engine.apply(GameEvent.NextRound)
        val result = engine.apply(GameEvent.NextRound)

        assertTrue(result.gameOver)
        assertEquals(RoundPhase.GAME_OVER, result.phase)
        assertEquals(BuzzState.CLOSED, result.buzzState)
    }

    @Test
    fun `a single-answer board ends the round straight from the face-off`() {
        val single = Question("solo", "سؤال", listOf(Answer("وحيد", 55)), "عام")
        val engine = GameEngine(freshState(questions = listOf(single), multipliers = listOf(1)))
        engine.buzzPodium(TeamId.TEAM_1)
        engine.correct(0)
        val result = engine.choosePlay()

        assertEquals(RoundPhase.ROUND_END, result.phase)
        assertEquals(55, result.score(TeamId.TEAM_1))
    }

    @Test
    fun `next round shows the scoreboard before the next question`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        repeat(3) { engine.wrong() }
        engine.wrong() // انتهت الجولة

        val scoreboard = engine.apply(GameEvent.NextRound)
        assertEquals(RoundPhase.SCOREBOARD, scoreboard.phase)
        // لسا نفس السؤال — بس بتظهر النتيجة.
        assertEquals(0, scoreboard.currentQuestionIndex)
        assertEquals(40, scoreboard.score(TeamId.TEAM_1))

        val next = engine.apply(GameEvent.NextRound)
        assertEquals(RoundPhase.FACE_OFF, next.phase)
        assertEquals(1, next.currentQuestionIndex)
    }
}
