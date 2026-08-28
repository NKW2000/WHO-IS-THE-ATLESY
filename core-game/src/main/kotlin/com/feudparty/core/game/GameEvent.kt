package com.feudparty.core.game

sealed class GameEvent {
    data class Buzz(val teamId: TeamId, val atMillis: Long) : GameEvent()
    data class JudgeCorrect(val answerIndex: Int) : GameEvent()
    object JudgeWrong : GameEvent()
    object NextQuestion : GameEvent()
    data class TeamJoined(val teamId: TeamId, val teamName: String) : GameEvent()
    data class TeamLeft(val teamId: TeamId) : GameEvent()
}
