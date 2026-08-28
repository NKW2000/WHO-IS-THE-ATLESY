package com.feudparty.core.game

import org.junit.Assert.assertEquals
import org.junit.Test

class GameEngineBuzzTest {
    private fun freshState() = GameState(
        questions = listOf(Question("q1", "سؤال", listOf(Answer("جواب", 50)), "عام")),
        teams = mapOf(
            TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "فريق ١"),
            TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "فريق ٢")
        )
    )

    @Test
    fun `first buzz locks the buzzer for that team`() {
        val engine = GameEngine(freshState())
        val result = engine.apply(GameEvent.Buzz(TeamId.TEAM_1, atMillis = 1000L))
        assertEquals(BuzzState.LOCKED_TEAM_1, result.buzzState)
    }

    @Test
    fun `second buzz is ignored once locked`() {
        val engine = GameEngine(freshState())
        engine.apply(GameEvent.Buzz(TeamId.TEAM_1, atMillis = 1000L))
        val result = engine.apply(GameEvent.Buzz(TeamId.TEAM_2, atMillis = 1050L))
        assertEquals(BuzzState.LOCKED_TEAM_1, result.buzzState)
    }
}
