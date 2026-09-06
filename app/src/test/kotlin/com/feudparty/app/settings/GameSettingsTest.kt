package com.feudparty.app.settings

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
    fun `the game needs one question per round`() {
        assertEquals(4, GameSettings(rounds = 4).questionsNeeded())
        assertEquals(7, GameSettings(rounds = 7).questionsNeeded())
    }

    @Test
    fun `out of range values are pulled back into range`() {
        val settings = GameSettings(rounds = 99, strikesToSteal = 0).clamped()

        assertEquals(GameSettings.MAX_ROUNDS, settings.rounds)
        assertEquals(GameSettings.MIN_STRIKES, settings.strikesToSteal)
    }

    @Test
    fun `an empty multiplier list falls back to the show's defaults`() {
        assertEquals(
            GameSettings.DEFAULT_MULTIPLIERS,
            GameSettings(multipliers = emptyList()).clamped().multipliers
        )
    }
}
