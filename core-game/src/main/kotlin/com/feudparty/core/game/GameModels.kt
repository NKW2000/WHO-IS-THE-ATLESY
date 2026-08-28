package com.feudparty.core.game

import kotlinx.serialization.Serializable

@Serializable
data class Answer(
    val text: String,
    val points: Int,
    val revealed: Boolean = false
)

@Serializable
data class Question(
    val id: String,
    val text: String,
    val answers: List<Answer>,
    val category: String
)

@Serializable
enum class TeamId { TEAM_1, TEAM_2 }

@Serializable
data class TeamState(
    val id: TeamId,
    val name: String,
    val score: Int = 0,
    val connected: Boolean = false
)

@Serializable
enum class BuzzState {
    OPEN,
    LOCKED_TEAM_1,
    LOCKED_TEAM_2,
    CLOSED
}

@Serializable
data class GameState(
    val questions: List<Question>,
    val currentQuestionIndex: Int = 0,
    val teams: Map<TeamId, TeamState>,
    val buzzState: BuzzState = BuzzState.OPEN,
    val roundOver: Boolean = false,
    val gameOver: Boolean = false
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentQuestionIndex)
}
