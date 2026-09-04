package com.feudparty.app.settings

import com.feudparty.core.game.FastMoneyState
import org.junit.Assert.assertEquals
import org.junit.Test

class GameSettingsTest {

    @Test
    fun `multipliers stretch to cover every round`() {
        val settings = GameSettings(rounds = 6, multipliers = listOf(1, 2, 3))

        // آخر مضاعف بينكرر لباقي الجولات.
        assertEquals(listOf(1, 2, 3, 3, 3, 3), settings.multipliersForRounds())
    }

    @Test
    fun `multipliers are trimmed when there are fewer rounds`() {
        val settings = GameSettings(rounds = 2, multipliers = listOf(1, 1, 2, 3))

        assertEquals(listOf(1, 1), settings.multipliersForRounds())
    }

    @Test
    fun `the game needs a question per round plus the fast money five`() {
        assertEquals(
            4 + FastMoneyState.QUESTIONS_PER_PLAYER,
            GameSettings(rounds = 4, fastMoneyEnabled = true).questionsNeeded()
        )
        assertEquals(4, GameSettings(rounds = 4, fastMoneyEnabled = false).questionsNeeded())
    }

    @Test
    fun `out of range values are pulled back into range`() {
        val settings = GameSettings(
            rounds = 99,
            strikesToSteal = 0,
            fastMoneyTarget = 5_000,
            fastMoneyFirstSeconds = 1,
            fastMoneySecondSeconds = 900
        ).clamped()

        assertEquals(GameSettings.MAX_ROUNDS, settings.rounds)
        assertEquals(GameSettings.MIN_STRIKES, settings.strikesToSteal)
        assertEquals(500, settings.fastMoneyTarget)
        assertEquals(10, settings.fastMoneyFirstSeconds)
        assertEquals(90, settings.fastMoneySecondSeconds)
    }

    @Test
    fun `an empty multiplier list falls back to the show's defaults`() {
        assertEquals(
            GameSettings.DEFAULT_MULTIPLIERS,
            GameSettings(multipliers = emptyList()).clamped().multipliers
        )
    }
}
