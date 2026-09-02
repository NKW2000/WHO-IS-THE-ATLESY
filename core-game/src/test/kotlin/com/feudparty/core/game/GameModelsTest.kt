package com.feudparty.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameModelsTest {
    @Test
    fun `currentQuestion returns question at currentQuestionIndex`() {
        val state = freshState().copy(currentQuestionIndex = 1)
        assertEquals("q2", state.currentQuestion!!.id)
    }

    @Test
    fun `currentQuestion returns null when index out of range`() {
        assertNull(freshState(questions = emptyList()).currentQuestion)
    }

    @Test
    fun `multiplier follows the round and sticks to the last value`() {
        val state = freshState(multipliers = listOf(1, 2, 3))
        assertEquals(1, state.multiplier)
        assertEquals(2, state.copy(currentQuestionIndex = 1).multiplier)
        assertEquals(3, state.copy(currentQuestionIndex = 9).multiplier)
    }

    @Test
    fun `active team follows the phase`() {
        val state = freshState()
        assertEquals(
            TeamId.TEAM_1,
            state.copy(phase = RoundPhase.FACE_OFF, faceOffTeam = TeamId.TEAM_1).activeTeam
        )
        assertEquals(
            TeamId.TEAM_2,
            state.copy(phase = RoundPhase.PLAY, controllingTeam = TeamId.TEAM_2).activeTeam
        )
        assertEquals(
            TeamId.TEAM_1,
            state.copy(phase = RoundPhase.STEAL, controllingTeam = TeamId.TEAM_2).activeTeam
        )
        assertNull(state.copy(phase = RoundPhase.ROUND_END).activeTeam)
    }

    @Test
    fun `masked state hides the question and the unrevealed answers from players`() {
        val engine = GameEngine(freshState())
        engine.buzzPodium(TeamId.TEAM_1)
        val masked = engine.correct(0).maskedForPlayers()
        val question = masked.currentQuestion!!

        // اللاعب ما بيشوف السؤال أصلاً — بيسمعه من المضيف.
        assertEquals("", question.text)
        assertEquals("الأول", question.answers[0].text)
        assertTrue(question.answers.drop(1).all { it.text.isEmpty() })
        // النقاط بتضل ظاهرة — اللوح بيعرض قيمة كل خانة مخفية.
        assertEquals(30, question.answers[1].points)
    }

    @Test
    fun `masked state hides fast money questions until they are revealed`() {
        val fastMoney = FastMoneyState(
            questions = listOf(board("f1")),
            teamId = TeamId.TEAM_1
        )
        val state = freshState().copy(
            fastMoneyQuestions = listOf(board("f1")),
            fastMoney = fastMoney
        )

        val masked = state.maskedForPlayers()
        assertTrue(masked.fastMoneyQuestions.isEmpty())
        assertTrue(masked.fastMoney!!.questions[0].answers.all { it.text.isEmpty() })

        val revealed = state.copy(fastMoney = fastMoney.copy(revealed = true)).maskedForPlayers()
        assertEquals("الأول", revealed.fastMoney!!.questions[0].answers[0].text)
    }
}
