package com.feudparty.core.game

/**
 * محرك قواعد اللعبة — كوتلن نقي، بدون أي اعتماد على أندرويد.
 * المضيف هو الوحيد اللي بيشغّل هالمحرك؛ باقي الأجهزة بس بتستقبل الحالة.
 */
class GameEngine(initialState: GameState) {
    var state: GameState = initialState
        private set

    fun apply(event: GameEvent): GameState {
        state = when (event) {
            is GameEvent.Buzz -> handleBuzz(event)
            is GameEvent.JudgeCorrect -> handleCorrect(event.answerIndex)
            GameEvent.JudgeWrong -> handleWrong()
            GameEvent.NextQuestion -> handleNext()
            is GameEvent.TeamJoined -> handleTeamJoined(event)
            is GameEvent.TeamLeft -> handleTeamLeft(event)
        }
        return state
    }

    private fun handleBuzz(event: GameEvent.Buzz): GameState {
        if (state.buzzState != BuzzState.OPEN) return state
        val locked = when (event.teamId) {
            TeamId.TEAM_1 -> BuzzState.LOCKED_TEAM_1
            TeamId.TEAM_2 -> BuzzState.LOCKED_TEAM_2
        }
        return state.copy(buzzState = locked)
    }

    private fun handleCorrect(answerIndex: Int): GameState {
        val question = state.currentQuestion ?: return state
        val answer = question.answers.getOrNull(answerIndex) ?: return state
        if (answer.revealed) return state
        val buzzedTeam = teamFromBuzzState(state.buzzState) ?: return state

        val updatedAnswers = question.answers.toMutableList()
        updatedAnswers[answerIndex] = answer.copy(revealed = true)
        val updatedQuestion = question.copy(answers = updatedAnswers)
        val updatedQuestions = state.questions.toMutableList()
        updatedQuestions[state.currentQuestionIndex] = updatedQuestion

        val updatedTeams = state.teams.toMutableMap()
        val team = updatedTeams.getValue(buzzedTeam)
        updatedTeams[buzzedTeam] = team.copy(score = team.score + answer.points)

        return state.copy(
            questions = updatedQuestions,
            teams = updatedTeams,
            buzzState = BuzzState.OPEN,
            roundOver = updatedAnswers.all { it.revealed }
        )
    }

    private fun handleWrong(): GameState = state.copy(buzzState = BuzzState.OPEN)

    private fun handleNext(): GameState {
        val nextIndex = state.currentQuestionIndex + 1
        return if (nextIndex >= state.questions.size) {
            state.copy(gameOver = true, roundOver = true, buzzState = BuzzState.CLOSED)
        } else {
            state.copy(
                currentQuestionIndex = nextIndex,
                buzzState = BuzzState.OPEN,
                roundOver = false
            )
        }
    }

    private fun handleTeamJoined(event: GameEvent.TeamJoined): GameState {
        val team = state.teams[event.teamId] ?: return state
        val updatedTeams = state.teams.toMutableMap()
        updatedTeams[event.teamId] = team.copy(
            name = event.teamName.ifBlank { team.name },
            connected = true
        )
        return state.copy(teams = updatedTeams)
    }

    private fun handleTeamLeft(event: GameEvent.TeamLeft): GameState {
        val team = state.teams[event.teamId] ?: return state
        val updatedTeams = state.teams.toMutableMap()
        updatedTeams[event.teamId] = team.copy(connected = false)
        return state.copy(teams = updatedTeams)
    }

    private fun teamFromBuzzState(buzzState: BuzzState): TeamId? = when (buzzState) {
        BuzzState.LOCKED_TEAM_1 -> TeamId.TEAM_1
        BuzzState.LOCKED_TEAM_2 -> TeamId.TEAM_2
        else -> null
    }
}
