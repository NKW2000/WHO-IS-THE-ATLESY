package com.feudparty.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTeamsTest {
    private fun freshState() = GameState(
        questions = listOf(Question("q1", "سؤال", listOf(Answer("جواب", 50)), "عام")),
        teams = mapOf(
            TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "فريق ١"),
            TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "فريق ٢")
        )
    )

    @Test
    fun `team joined marks team connected and renames it`() {
        val engine = GameEngine(freshState())
        val result = engine.apply(GameEvent.TeamJoined(TeamId.TEAM_2, "فريق النجوم"))
        val team = result.teams.getValue(TeamId.TEAM_2)
        assertTrue(team.connected)
        assertEquals("فريق النجوم", team.name)
    }

    @Test
    fun `team joined with blank name keeps the default name`() {
        val engine = GameEngine(freshState())
        val result = engine.apply(GameEvent.TeamJoined(TeamId.TEAM_1, "   "))
        assertEquals("فريق ١", result.teams.getValue(TeamId.TEAM_1).name)
    }

    @Test
    fun `team left marks team disconnected but keeps its score`() {
        val engine = GameEngine(freshState())
        engine.apply(GameEvent.TeamJoined(TeamId.TEAM_1, "فريق النجوم"))
        engine.apply(GameEvent.Buzz(TeamId.TEAM_1, atMillis = 1L))
        engine.apply(GameEvent.JudgeCorrect(0))
        val result = engine.apply(GameEvent.TeamLeft(TeamId.TEAM_1))
        val team = result.teams.getValue(TeamId.TEAM_1)
        assertFalse(team.connected)
        assertEquals(50, team.score)
    }

    @Test
    fun `revealing an already revealed answer does not award points twice`() {
        val engine = GameEngine(freshState())
        engine.apply(GameEvent.Buzz(TeamId.TEAM_1, atMillis = 1L))
        engine.apply(GameEvent.JudgeCorrect(0))
        engine.apply(GameEvent.Buzz(TeamId.TEAM_2, atMillis = 2L))
        val result = engine.apply(GameEvent.JudgeCorrect(0))
        assertEquals(50, result.teams.getValue(TeamId.TEAM_1).score)
        assertEquals(0, result.teams.getValue(TeamId.TEAM_2).score)
    }

    @Test
    fun `round is over once every answer is revealed`() {
        val state = freshState().copy(
            questions = listOf(
                Question("q1", "سؤال", listOf(Answer("أ", 50), Answer("ب", 30)), "عام")
            )
        )
        val engine = GameEngine(state)
        engine.apply(GameEvent.Buzz(TeamId.TEAM_1, atMillis = 1L))
        assertFalse(engine.apply(GameEvent.JudgeCorrect(0)).roundOver)
        engine.apply(GameEvent.Buzz(TeamId.TEAM_1, atMillis = 2L))
        assertTrue(engine.apply(GameEvent.JudgeCorrect(1)).roundOver)
    }
}
