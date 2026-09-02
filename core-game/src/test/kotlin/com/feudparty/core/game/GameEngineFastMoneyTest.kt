package com.feudparty.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineFastMoneyTest {

    private fun engineAtFastMoney(): GameEngine {
        val engine = GameEngine(
            freshState(
                questions = listOf(board("q1")),
                multipliers = listOf(1),
                fastMoneyQuestions = List(FastMoneyState.QUESTIONS_PER_PLAYER) { board("f$it") }
            )
        )
        // فريق ٢ بياخد الجولة فبيصير هو المتصدّر.
        engine.giveControlTo(TeamId.TEAM_2)
        engine.correct(1)
        engine.correct(2)
        engine.correct(3)
        engine.apply(GameEvent.NextRound)
        return engine
    }

    private fun GameEngine.fm(): FastMoneyState = state.fastMoney!!

    @Test
    fun `fast money starts for the leading team`() {
        val engine = engineAtFastMoney()

        assertEquals(RoundPhase.FAST_MONEY, engine.state.phase)
        assertEquals(TeamId.TEAM_2, engine.fm().teamId)
        assertEquals(FastMoneyState.QUESTIONS_PER_PLAYER, engine.fm().questions.size)
        assertEquals(FastMoneyState.FIRST_PLAYER_SECONDS, engine.fm().secondsRemaining)
        assertFalse(engine.fm().timerRunning)
    }

    @Test
    fun `five answers hand over to the second player with a longer clock`() {
        val engine = engineAtFastMoney()
        repeat(FastMoneyState.QUESTIONS_PER_PLAYER) { engine.apply(GameEvent.FastMoneySubmit(1)) }

        val fm = engine.fm()
        assertEquals(1, fm.playerIndex)
        assertEquals(0, fm.questionIndex)
        assertEquals(FastMoneyState.SECOND_PLAYER_SECONDS, fm.secondsRemaining)
        assertEquals(150, fm.playerOneTotal) // ٥ × ٣٠
        assertFalse(fm.finished)
    }

    @Test
    fun `second player cannot reuse the first player's answer`() {
        val engine = engineAtFastMoney()
        repeat(FastMoneyState.QUESTIONS_PER_PLAYER) { engine.apply(GameEvent.FastMoneySubmit(0)) }
        val flagged = engine.apply(GameEvent.FastMoneySubmit(0)).fastMoney!!

        assertTrue(flagged.duplicateFlag)
        assertEquals(0, flagged.playerTwo.size)
        assertEquals(0, flagged.questionIndex)

        val accepted = engine.apply(GameEvent.FastMoneySubmit(1)).fastMoney!!
        assertFalse(accepted.duplicateFlag)
        assertEquals(1, accepted.playerTwo.size)
        assertEquals(30, accepted.playerTwoTotal)
    }

    @Test
    fun `a pass records zero points and moves on`() {
        val engine = engineAtFastMoney()
        val fm = engine.apply(GameEvent.FastMoneySubmit(null)).fastMoney!!

        assertEquals(1, fm.questionIndex)
        assertEquals(0, fm.playerOneTotal)
        assertTrue(fm.playerOne.first().passed)
    }

    @Test
    fun `the clock ticks only while it is running`() {
        val engine = engineAtFastMoney()
        engine.apply(GameEvent.FastMoneyTick)
        assertEquals(FastMoneyState.FIRST_PLAYER_SECONDS, engine.fm().secondsRemaining)

        engine.apply(GameEvent.FastMoneyStartTimer)
        engine.apply(GameEvent.FastMoneyTick)
        assertEquals(FastMoneyState.FIRST_PLAYER_SECONDS - 1, engine.fm().secondsRemaining)
    }

    @Test
    fun `running out of time passes the remaining questions and hands over`() {
        val engine = engineAtFastMoney()
        engine.apply(GameEvent.FastMoneySubmit(0))
        engine.apply(GameEvent.FastMoneyStartTimer)
        repeat(FastMoneyState.FIRST_PLAYER_SECONDS) { engine.apply(GameEvent.FastMoneyTick) }

        val fm = engine.fm()
        assertEquals(FastMoneyState.QUESTIONS_PER_PLAYER, fm.playerOne.size)
        assertEquals(40, fm.playerOneTotal)
        assertEquals(1, fm.playerIndex)
        assertFalse(fm.timerRunning)
    }

    @Test
    fun `both players done marks the round finished`() {
        val engine = engineAtFastMoney()
        repeat(FastMoneyState.QUESTIONS_PER_PLAYER) { engine.apply(GameEvent.FastMoneySubmit(0)) }
        repeat(FastMoneyState.QUESTIONS_PER_PLAYER) { engine.apply(GameEvent.FastMoneySubmit(1)) }

        val fm = engine.fm()
        assertTrue(fm.finished)
        assertEquals(350, fm.total)
        assertTrue(fm.won)
    }

    @Test
    fun `reveal adds the fast money total to the team score once`() {
        val engine = engineAtFastMoney()
        val scoreBefore = engine.state.score(TeamId.TEAM_2)
        repeat(FastMoneyState.QUESTIONS_PER_PLAYER) { engine.apply(GameEvent.FastMoneySubmit(3)) }
        repeat(FastMoneyState.QUESTIONS_PER_PLAYER) { engine.apply(GameEvent.FastMoneySubmit(2)) }

        val revealed = engine.apply(GameEvent.FastMoneyReveal)
        assertTrue(revealed.fastMoney!!.revealed)
        assertEquals(scoreBefore + 150, revealed.score(TeamId.TEAM_2))
        assertFalse(revealed.fastMoney!!.won) // ١٥٠ < ٢٠٠

        val again = engine.apply(GameEvent.FastMoneyReveal)
        assertEquals(scoreBefore + 150, again.score(TeamId.TEAM_2))
    }

    @Test
    fun `end game closes everything`() {
        val engine = engineAtFastMoney()
        val result = engine.apply(GameEvent.EndGame)

        assertTrue(result.gameOver)
        assertEquals(RoundPhase.GAME_OVER, result.phase)
    }
}
