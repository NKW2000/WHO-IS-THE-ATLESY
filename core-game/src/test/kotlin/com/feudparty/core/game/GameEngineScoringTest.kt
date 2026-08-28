package com.feudparty.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineScoringTest {
    private fun lockedState() = GameState(
        questions = listOf(
            Question("q1", "سؤال١", listOf(Answer("جواب", 50)), "عام"),
            Question("q2", "سؤال٢", listOf(Answer("جواب٢", 30)), "عام")
        ),
        teams = mapOf(
            TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "فريق ١"),
            TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "فريق ٢")
        ),
        buzzState = BuzzState.LOCKED_TEAM_1
    )

    @Test
    fun `correct answer awards points to buzzed team and reveals answer`() {
        val engine = GameEngine(lockedState())
        val result = engine.apply(GameEvent.JudgeCorrect(answerIndex = 0))
        assertEquals(50, result.teams.getValue(TeamId.TEAM_1).score)
        assertTrue(result.currentQuestion!!.answers[0].revealed)
        assertEquals(BuzzState.OPEN, result.buzzState)
    }

    @Test
    fun `wrong answer reopens buzzer without awarding points`() {
        val engine = GameEngine(lockedState())
        val result = engine.apply(GameEvent.JudgeWrong)
        assertEquals(0, result.teams.getValue(TeamId.TEAM_1).score)
        assertEquals(BuzzState.OPEN, result.buzzState)
    }

    @Test
    fun `next question advances index and resets buzz state`() {
        val engine = GameEngine(lockedState())
        val result = engine.apply(GameEvent.NextQuestion)
        assertEquals(1, result.currentQuestionIndex)
        assertEquals(BuzzState.OPEN, result.buzzState)
    }

    @Test
    fun `next question on last question ends the game`() {
        val engine = GameEngine(lockedState().copy(currentQuestionIndex = 1))
        val result = engine.apply(GameEvent.NextQuestion)
        assertTrue(result.gameOver)
    }
}
