package com.feudparty.app.demo

import com.feudparty.core.game.Answer
import com.feudparty.core.game.BuzzState
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Player
import com.feudparty.core.game.Question
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState

/**
 * حالة لعبة مزيّفة لمعرض الشاشات — نفس شكل الحالة الحقيقية، بس بدون
 * شبكة ولا محرّك. كل شاشة بالمعرض بتاخد نسخة منها بالمرحلة اللي بدها ياها.
 */
internal val demoPlayers = listOf(
    Player("a1", "عبد الرحمن", TeamId.TEAM_1, seat = 1),
    Player("a2", "هناء", TeamId.TEAM_1, seat = 2),
    Player("a3", "زيد", TeamId.TEAM_1, seat = 3),
    Player("b1", "ليلى", TeamId.TEAM_2, seat = 1),
    Player("b2", "سامر", TeamId.TEAM_2, seat = 2)
)

internal fun demoState(
    phase: RoundPhase = RoundPhase.PLAY,
    revealed: Int = 1,
    matchStarted: Boolean = true,
    gameOver: Boolean = false,
    strikes: Int = 2,
    maskQuestion: Boolean = false
): GameState {
    val answers = listOf(
        "يشيّكوا الموبايل" to 40,
        "يشربوا قهوة" to 28,
        "يغسلوا وجّهم" to 16,
        "يصلّوا" to 9,
        "يفتحوا الشباك" to 5,
        "يرجعوا يناموا" to 2
    ).mapIndexed { index, (text, points) ->
        val open = index < revealed
        Answer(
            text = if (open || !maskQuestion) text else "",
            points = if (open || !maskQuestion) points else 0,
            revealed = open
        )
    }

    return GameState(
        questions = listOf(
            Question(
                id = "demo",
                text = if (maskQuestion) "" else "اذكر شي بيعمله الناس أول ما يصحوا من النوم",
                answers = answers,
                category = "عام"
            )
        ),
        players = demoPlayers,
        teams = mapOf(
            TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "نمور الشام", 140, connected = true),
            TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "صقور البحر", 95, connected = true)
        ),
        phase = phase,
        controllingTeam = TeamId.TEAM_1,
        faceOffTeam = TeamId.TEAM_1,
        faceOffWinner = TeamId.TEAM_1,
        turnPlayerId = when (phase) {
            RoundPhase.PLAY, RoundPhase.STEAL -> "a2"
            else -> null
        },
        buzzState = if (phase == RoundPhase.FACE_OFF) BuzzState.OPEN else BuzzState.CLOSED,
        pot = 40,
        strikes = strikes,
        answerSecondsLeft = if (phase == RoundPhase.PLAY) 7 else 0,
        choiceSecondsLeft = if (phase == RoundPhase.PLAY_OR_PASS) 4 else 0,
        roundWinner = TeamId.TEAM_1,
        matchStarted = matchStarted,
        gameOver = gameOver
    )
}
