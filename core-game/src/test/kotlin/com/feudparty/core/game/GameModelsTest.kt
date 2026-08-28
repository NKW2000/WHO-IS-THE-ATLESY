package com.feudparty.core.game

import org.junit.Assert.assertEquals
import org.junit.Test

class GameModelsTest {
    @Test
    fun `currentQuestion returns question at currentQuestionIndex`() {
        val q1 = Question("q1", "سؤال واحد", listOf(Answer("جواب", 50)), "عام")
        val q2 = Question("q2", "سؤال اثنين", listOf(Answer("جواب٢", 30)), "عام")
        val state = GameState(
            questions = listOf(q1, q2),
            currentQuestionIndex = 1,
            teams = mapOf(
                TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "فريق ١"),
                TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "فريق ٢")
            )
        )
        assertEquals(q2, state.currentQuestion)
    }

    @Test
    fun `currentQuestion returns null when index out of range`() {
        val state = GameState(
            questions = emptyList(),
            teams = mapOf(
                TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "فريق ١"),
                TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "فريق ٢")
            )
        )
        assertEquals(null, state.currentQuestion)
    }
}
