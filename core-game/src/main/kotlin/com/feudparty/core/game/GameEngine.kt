package com.feudparty.core.game

/**
 * محرك قواعد اللعبة — كوتلن نقي، بدون أي اعتماد على أندرويد.
 * المضيف هو الوحيد اللي بيشغّل هالمحرك؛ باقي الأجهزة بس بتستقبل الحالة.
 *
 * تسلسل الجولة زي البرنامج:
 * 1. **المواجهة**: الزر مفتوح للفريقين. أول بزّة بتجاوب؛ إذا طلع جوابها
 *    الجواب رقم ١ بتاخد اللوح فوراً، وإلا الفريق التاني بياخد فرصة يتفوّق.
 * 2. **اللعب**: الفريق اللي فاز بالمواجهة بيكشف اللوح لحاله. كل غلط = X،
 *    وثلاث X بتفتح فرصة السرقة.
 * 3. **السرقة**: محاولة وحدة للفريق المقابل — إذا صحّت بياخد كل نقاط الجولة.
 * 4. نقاط الجولة (الـ pot) بتتضرب بمضاعف الجولة وبتروح لفريق واحد بس.
 */
class GameEngine(initialState: GameState) {
    var state: GameState = initialState
        private set

    fun apply(event: GameEvent): GameState {
        state = when (event) {
            is GameEvent.Buzz -> handleBuzz(event)
            is GameEvent.JudgeCorrect -> handleCorrect(event.answerIndex)
            GameEvent.JudgeWrong -> handleWrong()
            GameEvent.NextRound -> handleNextRound()
            is GameEvent.TeamJoined -> handleTeamJoined(event)
            is GameEvent.TeamLeft -> handleTeamLeft(event)
            GameEvent.FastMoneyStartTimer -> withFastMoney { it.copy(timerRunning = true) }
            GameEvent.FastMoneyTick -> handleFastMoneyTick()
            is GameEvent.FastMoneySubmit -> handleFastMoneySubmit(event.answerIndex)
            GameEvent.FastMoneyReveal -> handleFastMoneyReveal()
            GameEvent.EndGame -> state.copy(
                phase = RoundPhase.GAME_OVER,
                gameOver = true,
                buzzState = BuzzState.CLOSED
            )
        }
        return state
    }

    // ---------------------------------------------------------------- المواجهة

    private fun handleBuzz(event: GameEvent.Buzz): GameState {
        if (state.phase != RoundPhase.FACE_OFF) return state
        if (state.buzzState != BuzzState.OPEN) return state
        val locked = when (event.teamId) {
            TeamId.TEAM_1 -> BuzzState.LOCKED_TEAM_1
            TeamId.TEAM_2 -> BuzzState.LOCKED_TEAM_2
        }
        return state.copy(buzzState = locked, faceOffTeam = event.teamId)
    }

    // ------------------------------------------------------------- حكم المضيف

    private fun handleCorrect(answerIndex: Int): GameState {
        val question = state.currentQuestion ?: return state
        val answer = question.answers.getOrNull(answerIndex) ?: return state
        if (answer.revealed) return state

        return when (state.phase) {
            RoundPhase.FACE_OFF -> faceOffCorrect(answerIndex, answer, first = true)
            RoundPhase.FACE_OFF_SECOND -> faceOffCorrect(answerIndex, answer, first = false)
            RoundPhase.PLAY -> playCorrect(answerIndex, answer)
            RoundPhase.STEAL -> stealCorrect(answerIndex, answer)
            else -> state
        }
    }

    private fun faceOffCorrect(index: Int, answer: Answer, first: Boolean): GameState {
        val team = state.faceOffTeam ?: return state
        val revealed = reveal(index).copy(pot = state.pot + answer.points)

        if (first) {
            // جواب رقم ١ بياخد اللوح على طول، غيره بيفتح فرصة للفريق التاني.
            return if (index == 0) {
                revealed.startPlay(team)
            } else {
                revealed.copy(
                    phase = RoundPhase.FACE_OFF_SECOND,
                    buzzState = BuzzState.CLOSED,
                    faceOffTeam = team.other(),
                    faceOffLeader = team,
                    faceOffLeaderPoints = answer.points
                )
            }
        }

        val leader = revealed.faceOffLeader
        val winner = when {
            leader == null -> team
            answer.points > revealed.faceOffLeaderPoints -> team
            else -> leader
        }
        return revealed.startPlay(winner)
    }

    private fun playCorrect(index: Int, answer: Answer): GameState {
        val team = state.controllingTeam ?: return state
        val revealed = reveal(index).copy(pot = state.pot + answer.points)
        return if (revealed.allRevealed()) revealed.award(team, stolen = false) else revealed
    }

    private fun stealCorrect(index: Int, answer: Answer): GameState {
        val thief = state.stealingTeam ?: return state
        val revealed = reveal(index).copy(pot = state.pot + answer.points)
        return revealed.award(thief, stolen = true)
    }

    private fun handleWrong(): GameState = when (state.phase) {
        RoundPhase.FACE_OFF -> {
            val team = state.faceOffTeam
            if (team == null) {
                state
            } else {
                state.copy(
                    phase = RoundPhase.FACE_OFF_SECOND,
                    buzzState = BuzzState.CLOSED,
                    faceOffTeam = team.other()
                )
            }
        }

        RoundPhase.FACE_OFF_SECOND -> {
            val leader = state.faceOffLeader
            if (leader != null) {
                state.startPlay(leader)
            } else {
                // الاتنين غلطوا — منرجّع الزر مفتوح لنفس السؤال.
                state.copy(
                    phase = RoundPhase.FACE_OFF,
                    buzzState = BuzzState.OPEN,
                    faceOffTeam = null
                )
            }
        }

        RoundPhase.PLAY -> {
            val strikes = state.strikes + 1
            if (strikes >= STRIKES_TO_STEAL) {
                state.copy(strikes = strikes, phase = RoundPhase.STEAL, buzzState = BuzzState.CLOSED)
            } else {
                state.copy(strikes = strikes)
            }
        }

        RoundPhase.STEAL -> {
            val owner = state.controllingTeam
            if (owner == null) state else state.award(owner, stolen = false)
        }

        else -> state
    }

    // ---------------------------------------------------------- انتقال الجولات

    private fun handleNextRound(): GameState {
        val nextIndex = state.currentQuestionIndex + 1
        if (nextIndex < state.questions.size) {
            return state.copy(
                currentQuestionIndex = nextIndex,
                phase = RoundPhase.FACE_OFF,
                buzzState = BuzzState.OPEN,
                pot = 0,
                strikes = 0,
                controllingTeam = null,
                faceOffTeam = null,
                faceOffLeader = null,
                faceOffLeaderPoints = 0,
                roundWinner = null
            )
        }

        val fastMoneyQuestions = state.fastMoneyQuestions
            .take(FastMoneyState.QUESTIONS_PER_PLAYER)
        if (state.fastMoney == null && fastMoneyQuestions.size == FastMoneyState.QUESTIONS_PER_PLAYER) {
            return state.copy(
                phase = RoundPhase.FAST_MONEY,
                buzzState = BuzzState.CLOSED,
                strikes = 0,
                fastMoney = FastMoneyState(
                    questions = fastMoneyQuestions,
                    teamId = state.leadingTeam ?: TeamId.TEAM_1
                )
            )
        }

        return state.copy(phase = RoundPhase.GAME_OVER, gameOver = true, buzzState = BuzzState.CLOSED)
    }

    // ------------------------------------------------------------ اللعبة السريعة

    private fun handleFastMoneyTick(): GameState = withFastMoney { fm ->
        if (!fm.timerRunning || fm.finished) {
            fm
        } else {
            val remaining = fm.secondsRemaining - 1
            if (remaining > 0) fm.copy(secondsRemaining = remaining) else fm.timeUp()
        }
    }

    private fun handleFastMoneySubmit(answerIndex: Int?): GameState = withFastMoney { fm ->
        val question = fm.currentQuestion
        when {
            fm.finished || question == null -> fm

            // اللاعب التاني ما بينفع يكرر جواب اللاعب الأول — بينعاد سؤاله.
            fm.playerIndex == 1 && answerIndex != null && answerIndex in fm.usedByPlayerOne ->
                fm.copy(duplicateFlag = true)

            else -> {
                val points = answerIndex?.let { question.answers.getOrNull(it)?.points } ?: 0
                fm.record(FastMoneyEntry(answerIndex = answerIndex, points = points))
            }
        }
    }

    private fun handleFastMoneyReveal(): GameState {
        val fm = state.fastMoney ?: return state
        if (fm.revealed) return state
        val teams = state.teams.toMutableMap()
        teams[fm.teamId]?.let { team -> teams[fm.teamId] = team.copy(score = team.score + fm.total) }
        return state.copy(
            fastMoney = fm.copy(revealed = true, timerRunning = false, finished = true),
            teams = teams,
            lastAward = Award(fm.teamId, fm.total)
        )
    }

    private inline fun withFastMoney(block: (FastMoneyState) -> FastMoneyState): GameState {
        val fm = state.fastMoney ?: return state
        return state.copy(fastMoney = block(fm))
    }

    // ------------------------------------------------------------------- الفرق

    private fun handleTeamJoined(event: GameEvent.TeamJoined): GameState {
        val team = state.teams[event.teamId] ?: return state
        val updated = state.teams.toMutableMap()
        updated[event.teamId] = team.copy(
            name = event.teamName.ifBlank { team.name },
            connected = true
        )
        return state.copy(teams = updated)
    }

    private fun handleTeamLeft(event: GameEvent.TeamLeft): GameState {
        val team = state.teams[event.teamId] ?: return state
        val updated = state.teams.toMutableMap()
        updated[event.teamId] = team.copy(connected = false)
        return state.copy(teams = updated)
    }

    // ----------------------------------------------------------------- مساعدات

    private fun reveal(index: Int): GameState = state.mapCurrentQuestion { question ->
        val answers = question.answers.toMutableList()
        answers[index] = answers[index].copy(revealed = true)
        question.copy(answers = answers)
    }

    companion object {
        const val STRIKES_TO_STEAL = 3
    }
}

private fun GameState.mapCurrentQuestion(transform: (Question) -> Question): GameState {
    val question = currentQuestion ?: return this
    val updated = questions.toMutableList()
    updated[currentQuestionIndex] = transform(question)
    return copy(questions = updated)
}

private fun GameState.allRevealed(): Boolean =
    currentQuestion?.answers?.all { it.revealed } ?: false

private fun GameState.startPlay(team: TeamId): GameState {
    val next = copy(
        phase = RoundPhase.PLAY,
        controllingTeam = team,
        buzzState = BuzzState.CLOSED,
        strikes = 0,
        faceOffTeam = null
    )
    // لوح صغير ممكن يخلص من المواجهة نفسها.
    return if (next.allRevealed()) next.award(team, stolen = false) else next
}

/** بتقفل الجولة: كل النقاط × مضاعف الجولة لفريق واحد، وبتكشف باقي اللوح. */
private fun GameState.award(team: TeamId, stolen: Boolean): GameState {
    val points = pot * multiplier
    val updatedTeams = teams.toMutableMap()
    updatedTeams[team]?.let { updatedTeams[team] = it.copy(score = it.score + points) }
    return mapCurrentQuestion { question ->
        question.copy(answers = question.answers.map { it.copy(revealed = true) })
    }.copy(
        teams = updatedTeams,
        phase = RoundPhase.ROUND_END,
        buzzState = BuzzState.CLOSED,
        roundWinner = team,
        lastAward = Award(team, points, stolen)
    )
}

/** انتهى وقت اللاعب — باقي أسئلته بتتسجّل «ما جاوب». */
private fun FastMoneyState.timeUp(): FastMoneyState {
    val startingPlayer = playerIndex
    var current = copy(timerRunning = false, secondsRemaining = 0)
    while (!current.finished && current.playerIndex == startingPlayer) {
        current = current.record(FastMoneyEntry(answerIndex = null, points = 0))
    }
    return current
}

/** بتسجّل جواب وبتنقل للسؤال/اللاعب التالي. */
private fun FastMoneyState.record(entry: FastMoneyEntry): FastMoneyState {
    val updated = if (playerIndex == 0) {
        copy(playerOne = playerOne + entry)
    } else {
        copy(playerTwo = playerTwo + entry)
    }.copy(duplicateFlag = false)

    val nextQuestion = questionIndex + 1
    if (nextQuestion < questions.size) return updated.copy(questionIndex = nextQuestion)

    return if (playerIndex == 0) {
        updated.copy(
            playerIndex = 1,
            questionIndex = 0,
            secondsRemaining = FastMoneyState.SECOND_PLAYER_SECONDS,
            timerRunning = false
        )
    } else {
        updated.copy(finished = true, timerRunning = false)
    }
}
