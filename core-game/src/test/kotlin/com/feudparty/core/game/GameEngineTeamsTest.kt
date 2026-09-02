package com.feudparty.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTeamsTest {
    private fun soloBoard() = freshState(
        questions = listOf(Question("q1", "سؤال", listOf(Answer("جواب", 50)), "عام")),
        multipliers = listOf(1)
    )

    @Test
    fun `team joined marks team connected and renames it`() {
        val engine = GameEngine(soloBoard())
        val result = engine.apply(GameEvent.TeamJoined(TeamId.TEAM_2, "فريق النجوم"))
        val team = result.teams.getValue(TeamId.TEAM_2)
        assertTrue(team.connected)
        assertEquals("فريق النجوم", team.name)
    }

    @Test
    fun `team joined with blank name keeps the default name`() {
        val engine = GameEngine(soloBoard())
        val result = engine.apply(GameEvent.TeamJoined(TeamId.TEAM_1, "   "))
        assertEquals("فريق ١", result.teams.getValue(TeamId.TEAM_1).name)
    }

    @Test
    fun `team left marks team disconnected but keeps its score`() {
        val engine = GameEngine(soloBoard())
        engine.giveControlTo(TeamId.TEAM_1) // لوح من جواب واحد فبتنتهي الجولة فوراً
        val result = engine.apply(GameEvent.TeamLeft(TeamId.TEAM_1))
        val team = result.teams.getValue(TeamId.TEAM_1)

        assertFalse(team.connected)
        assertEquals(50, team.score)
    }

    @Test
    fun `judging after the round ended changes nothing`() {
        val engine = GameEngine(soloBoard())
        engine.giveControlTo(TeamId.TEAM_1)
        val result = engine.correct(0)

        assertEquals(50, result.score(TeamId.TEAM_1))
        assertEquals(0, result.score(TeamId.TEAM_2))
        assertEquals(RoundPhase.ROUND_END, result.phase)
    }

    @Test
    fun `buzzing while the buzzer is closed is ignored`() {
        val engine = GameEngine(soloBoard())
        engine.giveControlTo(TeamId.TEAM_1)
        val result = engine.buzz(TeamId.TEAM_2)

        assertEquals(BuzzState.CLOSED, result.buzzState)
    }
}
