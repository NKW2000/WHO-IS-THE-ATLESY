package com.feudparty.core.game

/**
 * محرك قواعد اللعبة — كوتلن نقي، بدون أي اعتماد على أندرويد.
 * المضيف هو الوحيد اللي بيشغّل هالمحرك؛ أجهزة اللاعبين بس بتستقبل الحالة.
 *
 * كل لاعب = جهاز. تسلسل الجولة زي البرنامج:
 * 1. **المواجهة**: لاعب المنصة من كل فريق بس بيقدر يضغط. أول ضغطة بتجاوب؛
 *    الجواب رقم ١ بياخد اللوح فوراً، وإلا الفريق التاني بياخد فرصة يتفوّق.
 * 2. **اللعب**: الفريق اللي فاز بالمواجهة بيجاوب **لاعب ورا لاعب بالدور** —
 *    اللي جاوب ما بيرجع دوره إلا لما يخلّص كل زمايله. كل غلط = X، وثلاث X
 *    بتفتح فرصة السرقة.
 * 3. **السرقة**: لاعب المنصة من الفريق المقابل عنده محاولة وحدة.
 * 4. نقاط الجولة بتتضرب بمضاعف الجولة وبتروح لفريق واحد بس.
 *
 * لاعب المنصة بيتغير كل جولة (`podiumIndex`) — زي ما بيتبدلوا عالمنصة
 * بالبرنامج.
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
            is GameEvent.PlayerJoined -> handlePlayerJoined(event)
            is GameEvent.PlayerLeft -> handlePlayerLeft(event)
            GameEvent.EndGame -> state.copy(
                phase = RoundPhase.GAME_OVER,
                gameOver = true,
                buzzState = BuzzState.CLOSED,
                buzzedPlayerId = null,
                turnPlayerId = null
            )
        }
        return state
    }

    // ------------------------------------------------------------------ الضغط

    private fun handleBuzz(event: GameEvent.Buzz): GameState {
        val player = state.player(event.playerId) ?: return state
        if (event.playerId !in state.armedPlayerIds()) return state

        return when (state.phase) {
            RoundPhase.FACE_OFF -> {
                if (state.buzzState != BuzzState.OPEN) return state
                val locked = when (player.teamId) {
                    TeamId.TEAM_1 -> BuzzState.LOCKED_TEAM_1
                    TeamId.TEAM_2 -> BuzzState.LOCKED_TEAM_2
                }
                state.copy(
                    buzzState = locked,
                    faceOffTeam = player.teamId,
                    buzzedPlayerId = player.id
                )
            }

            // بمراحل اللعب الضغطة بس بتوضّح إنه اللاعب عم يجاوب هلق.
            RoundPhase.FACE_OFF_SECOND, RoundPhase.PLAY, RoundPhase.STEAL ->
                state.copy(buzzedPlayerId = player.id)

            else -> state
        }
    }

    // ------------------------------------------------------------- حكم المضيف

    /** اللاعب اللي المضيف عم يحكم على جوابه هلق. */
    private fun answeringPlayerId(): String? = when (state.phase) {
        RoundPhase.FACE_OFF -> state.buzzedPlayerId
        RoundPhase.FACE_OFF_SECOND ->
            state.buzzedPlayerId ?: state.faceOffTeam?.let { state.podiumPlayer(it)?.id }

        RoundPhase.PLAY, RoundPhase.STEAL -> state.turnPlayerId
        else -> null
    }

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
        val revealed = reveal(index)
            .copy(pot = state.pot + answer.points)
            .markCorrect(answeringPlayerId())

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
                    faceOffLeaderPoints = answer.points,
                    buzzedPlayerId = null
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
        val revealed = reveal(index)
            .copy(pot = state.pot + answer.points)
            .markCorrect(answeringPlayerId())
        return if (revealed.allRevealed()) {
            revealed.award(team, stolen = false)
        } else {
            revealed.advanceTurn(team)
        }
    }

    private fun stealCorrect(index: Int, answer: Answer): GameState {
        val thief = state.stealingTeam ?: return state
        return reveal(index)
            .copy(pot = state.pot + answer.points)
            .markCorrect(answeringPlayerId())
            .award(thief, stolen = true)
    }

    private fun handleWrong(): GameState {
        val answering = answeringPlayerId()
        return when (state.phase) {
            RoundPhase.FACE_OFF -> {
                val team = state.faceOffTeam ?: return state
                state.markWrong(answering).copy(
                    phase = RoundPhase.FACE_OFF_SECOND,
                    buzzState = BuzzState.CLOSED,
                    faceOffTeam = team.other(),
                    buzzedPlayerId = null
                )
            }

            RoundPhase.FACE_OFF_SECOND -> {
                val marked = state.markWrong(answering)
                val leader = marked.faceOffLeader
                if (leader != null) {
                    marked.startPlay(leader)
                } else {
                    // الاتنين غلطوا — منرجّع الزر مفتوح لنفس السؤال.
                    marked.copy(
                        phase = RoundPhase.FACE_OFF,
                        buzzState = BuzzState.OPEN,
                        faceOffTeam = null,
                        buzzedPlayerId = null
                    )
                }
            }

            RoundPhase.PLAY -> {
                val team = state.controllingTeam ?: return state
                val strikes = state.strikes + 1
                val marked = state.markWrong(answering).copy(strikes = strikes)
                if (strikes >= state.strikesToSteal) marked.openSteal(team) else marked.advanceTurn(team)
            }

            RoundPhase.STEAL -> {
                val owner = state.controllingTeam ?: return state
                state.markWrong(answering).award(owner, stolen = false)
            }

            else -> state
        }
    }

    // ---------------------------------------------------------- انتقال الجولات

    private fun handleNextRound(): GameState {
        val nextIndex = state.currentQuestionIndex + 1
        if (nextIndex >= state.questions.size) {
            return state.copy(
                phase = RoundPhase.GAME_OVER,
                gameOver = true,
                buzzState = BuzzState.CLOSED,
                buzzedPlayerId = null,
                turnPlayerId = null
            )
        }

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
            buzzedPlayerId = null,
            turnPlayerId = null,
            wrongPlayers = emptySet(),
            correctPlayers = emptySet(),
            roundWinner = null,
            podiumIndex = state.rotatePodium()
        )
    }

    // --------------------------------------------------------------- اللاعبين

    private fun handlePlayerJoined(event: GameEvent.PlayerJoined): GameState {
        val existing = state.players.firstOrNull { it.id == event.playerId }
        val players = if (existing == null) {
            state.players + Player(event.playerId, event.name, event.teamId)
        } else {
            state.players.map {
                if (it.id == event.playerId) {
                    it.copy(name = event.name.ifBlank { it.name }, connected = true)
                } else {
                    it
                }
            }
        }
        val teams = state.teams.toMutableMap()
        teams[event.teamId]?.let { teams[event.teamId] = it.copy(connected = true) }
        return state.copy(players = players, teams = teams)
    }

    private fun handlePlayerLeft(event: GameEvent.PlayerLeft): GameState {
        val players = state.players.map {
            if (it.id == event.playerId) it.copy(connected = false) else it
        }
        val teams = state.teams.toMutableMap()
        TeamId.entries.forEach { teamId ->
            val anyConnected = players.any { it.teamId == teamId && it.connected }
            teams[teamId]?.let { teams[teamId] = it.copy(connected = anyConnected) }
        }
        return state.copy(players = players, teams = teams)
    }

    // ----------------------------------------------------------------- مساعدات

    private fun reveal(index: Int): GameState = state.mapCurrentQuestion { question ->
        val answers = question.answers.toMutableList()
        answers[index] = answers[index].copy(revealed = true)
        question.copy(answers = answers)
    }

    companion object {
        const val DEFAULT_STRIKES_TO_STEAL = 3
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

private fun GameState.markCorrect(playerId: String?): GameState =
    if (playerId == null) this
    else copy(correctPlayers = correctPlayers + playerId, wrongPlayers = wrongPlayers - playerId)

private fun GameState.markWrong(playerId: String?): GameState =
    if (playerId == null) this
    else copy(wrongPlayers = wrongPlayers + playerId, correctPlayers = correctPlayers - playerId)

/** بداية مرحلة اللعب: الدور بينتقل للاعب اللي بعد لاعب المنصة. */
private fun GameState.startPlay(team: TeamId): GameState {
    val next = copy(
        phase = RoundPhase.PLAY,
        controllingTeam = team,
        buzzState = BuzzState.CLOSED,
        strikes = 0,
        faceOffTeam = null,
        buzzedPlayerId = null,
        turnIndex = turnIndex + (team to (podiumIndex[team] ?: 0))
    ).advanceTurn(team)

    // لوح صغير ممكن يخلص من المواجهة نفسها.
    return if (next.allRevealed()) next.award(team, stolen = false) else next
}

/** الدور بينتقل للاعب اللي بعده بالفريق — واللي جاوب بيستنى دورة كاملة. */
private fun GameState.advanceTurn(team: TeamId): GameState {
    val list = playersOf(team)
    if (list.isEmpty()) return copy(turnPlayerId = null, buzzedPlayerId = null)

    val current = turnIndex[team] ?: 0
    val nextIndex = nextConnectedIndex(list, current)
    val nextPlayer = list[nextIndex]
    return copy(
        turnIndex = turnIndex + (team to nextIndex),
        turnPlayerId = nextPlayer.id,
        buzzedPlayerId = null,
        // أول ما يرجع دوره بترجع شاشته حيادية.
        wrongPlayers = wrongPlayers - nextPlayer.id,
        correctPlayers = correctPlayers - nextPlayer.id
    )
}

/** فرصة السرقة بتروح للاعب المنصة عند الفريق المقابل. */
private fun GameState.openSteal(controlling: TeamId): GameState {
    val thief = controlling.other()
    val podium = podiumPlayer(thief)
    return copy(
        phase = RoundPhase.STEAL,
        buzzState = BuzzState.CLOSED,
        buzzedPlayerId = null,
        turnPlayerId = podium?.id,
        turnIndex = if (podium == null) turnIndex else turnIndex + (thief to (podiumIndex[thief] ?: 0)),
        wrongPlayers = if (podium == null) wrongPlayers else wrongPlayers - podium.id,
        correctPlayers = if (podium == null) correctPlayers else correctPlayers - podium.id
    )
}

private fun GameState.nextConnectedIndex(list: List<Player>, current: Int): Int {
    for (step in 1..list.size) {
        val candidate = (current + step) % list.size
        if (list[candidate].connected) return candidate
    }
    return (current + 1) % list.size
}

/** لاعب المنصة بيتبدّل كل جولة. */
private fun GameState.rotatePodium(): Map<TeamId, Int> =
    TeamId.entries.associateWith { teamId ->
        val size = playersOf(teamId).size
        if (size == 0) 0 else ((podiumIndex[teamId] ?: 0) + 1) % size
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
        buzzedPlayerId = null,
        turnPlayerId = null,
        roundWinner = team,
        lastAward = Award(team, points, stolen)
    )
}


